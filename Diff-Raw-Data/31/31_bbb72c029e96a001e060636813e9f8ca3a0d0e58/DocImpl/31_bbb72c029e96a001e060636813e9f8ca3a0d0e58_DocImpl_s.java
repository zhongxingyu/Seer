 package kkckkc.jsourcepad.model;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 import kkckkc.jsourcepad.ScopeRoot;
 import kkckkc.jsourcepad.model.bundle.Bundle;
 import kkckkc.jsourcepad.model.bundle.BundleListener;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.jsourcepad.util.messagebus.Subscription;
 import kkckkc.syntaxpane.model.LineManager;
 import kkckkc.syntaxpane.model.SourceDocument;
 import kkckkc.syntaxpane.parse.grammar.Language;
 import kkckkc.syntaxpane.parse.grammar.LanguageManager;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 
 import javax.annotation.PreDestroy;
 import javax.swing.*;
 import java.io.*;
 import java.util.List;
 
 
 public class DocImpl implements Doc, ScopeRoot, BeanFactoryAware {
 	protected LanguageManager languageManager;
 	private DocList docList;
 	protected Window window;
 
 	protected File backingFile;
     protected long backingTimestamp;
 
 	protected BufferImpl buffer;
 	private TabManager tabManager;
 	private DefaultListableBeanFactory container;
     private SourceDocument sourceDocument;
     private List<Subscription> subscriptions = Lists.newArrayList();
 
     @Autowired
 	public DocImpl(final Window window, final DocList docList, LanguageManager languageManager) {
 		this.docList = docList;
 
         sourceDocument = new SourceDocument();
         this.buffer = new BufferImpl(sourceDocument, this, window);
 		this.buffer.setLanguage(languageManager.getLanguage(null));
 		
 		this.window = window;
 		this.languageManager = languageManager;
 		this.tabManager = new TabManagerImpl(this);
 
         subscriptions.add(Application.get().topic(BundleListener.class).subscribe(DispatchStrategy.ASYNC, new BundleListener() {
 
             @Override
             public void bundleAdded(Bundle bundle) {
             }
 
             @Override
             public void bundleRemoved(Bundle bundle) {
             }
 
             @Override
             public void bundleUpdated(Bundle bundle) {
             }
 
             @Override
             public void languagesUpdated() {
                 LineManager.Line line = buffer.getLineManager().getLineByPosition(0);
                 Language language = DocImpl.this.languageManager.getLanguage(
                         line.getCharSequence(false).toString(), backingFile);
                 DocImpl.this.buffer.setLanguage(language);
             }
         }));
 
         subscriptions.add(window.topic(Window.FocusListener.class).subscribe(DispatchStrategy.ASYNC_EVENT,
                 new Window.FocusListener() {
                     @Override
                     public void focusGained(Window window) {
                         if (docList.getActiveDoc() == DocImpl.this) {
                             checkFileForModification(window);
                         }
                     }
 
                     @Override
                     public void focusLost(Window window) {
                     }
                 }));
 
         subscriptions.add(window.topic(DocList.Listener.class).subscribe(DispatchStrategy.ASYNC_EVENT,
                 new DocList.Listener() {
                     @Override
                     public void created(Doc doc) {
                     }
 
                     @Override
                     public void selected(int index, Doc doc) {
                         if (doc == DocImpl.this) {
                             checkFileForModification(window);
                         }
                     }
 
                     @Override
                     public void closed(int index, Doc doc) {
                     }
                 }));
 	}
 
     public void refresh() {
         try {
             this.open(backingFile);
         } catch (IOException ioe) {
             throw new RuntimeException(ioe);
         }
     }
 
     private void checkFileForModification(Window window) {
         if (backingFile != null && backingTimestamp != backingFile.lastModified()) {
             int option = JOptionPane.showOptionDialog(window.getContainer(),
                     "File has been changed by an external process. Do you want to reload it?",
                     "File Changed",
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     new String[]{"Yes, Reload File", "No, Keep the Current Contents"},
                     "Yes, Reload File");
             if (option == 0) {
                 refresh();
             } else {
                 backingTimestamp = backingFile.lastModified();
             }
         }
     }
 
     @PreDestroy
     public void destroy() {
         for (Subscription subscription : subscriptions) {
             subscription.unsubscribe();
         }
     }
 
 	public Buffer getActiveBuffer() {
 		return buffer;
 	}
 	
 	@Override
 	public void close() {
 		docList.close(this);
 
         this.sourceDocument.close();
         this.sourceDocument = null;
 
         this.buffer.close();
         this.buffer = null;
 
         this.container.destroySingletons();
         this.container = null;
 	}
 
 	@Override
 	public String getTitle() {
 		return (isModified() ? "*" : "") + 
 			(backingFile == null ? "Untitled" : backingFile.getName());
 	}
 
 	@Override
 	public boolean isModified() {
         return getActiveBuffer() != null && getActiveBuffer().isModified();
     }
 
 	@Override
 	public boolean isBackedByFile() {
 		return backingFile != null;
 	}
 
 	@Override
 	public void save() {
 		try {
             Writer fw = new OutputStreamWriter(new FileOutputStream(this.backingFile), Charsets.UTF_8);
 			fw.write(buffer.getText(buffer.getCompleteDocument()));
 			fw.close();
 
             this.backingTimestamp = backingFile.lastModified();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		getActiveBuffer().clearModified();
 
         Project p = getDocList().getWindow().getProject();
         p.refresh(backingFile);
         p.refresh(backingFile.getParentFile());
 		window.topic(Doc.StateListener.class).post().modified(this, true, false);
 	}
 
 	@Override
 	public void saveAs(File file) {
 		try {
 			Writer fw = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
 			fw.write(buffer.getText(buffer.getCompleteDocument()));
 			fw.close();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		getActiveBuffer().clearModified();
 		
 		this.backingFile = file;
         this.backingTimestamp = file.lastModified();
 
         Project p = getDocList().getWindow().getProject();
         p.refresh(file);
         p.refresh(file.getParentFile());
 		window.topic(Doc.StateListener.class).post().modified(this, true, false);
 	}
 
 	@Override
 	public void open(File file) throws IOException {
 		this.backingFile = file;
         this.backingTimestamp = file.lastModified();
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
 		br.mark(1024);
 		
 		char[] buffer = new char[800];
 		br.read(buffer);
 		br.reset();
 		
 		Language language = languageManager.getLanguage(new String(buffer), file);
 		
 		this.buffer.setText(language, br);
 	}
 
 	
 	
 	@Override
 	public File getFile() {
 		return backingFile;
 	}
 
 	@Override
 	public void activate() {
 		InsertionPoint ip = this.buffer.getInsertionPoint();
 		window.topic(Buffer.InsertionPointListener.class).post().update(ip);
 	}
 
 	@Override
 	public DocList getDocList() {
 		return docList;
 	}
 	
 	@Override
 	public TabManager getTabManager() {
 	    return tabManager;
 	}
 
 	@Override
     public <T> T getPresenter(Class<? extends T> clazz) {
 	    return container.getBean(clazz);
     }
 
 
     @Override
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
 	    this.container = (DefaultListableBeanFactory) beanFactory;
     }
 	
 	@Override
 	public DefaultListableBeanFactory getBeanFactory() {
 	    return container;
 	}
 	
 }
