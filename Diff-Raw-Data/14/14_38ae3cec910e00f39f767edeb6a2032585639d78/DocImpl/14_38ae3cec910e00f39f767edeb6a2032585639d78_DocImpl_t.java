 package kkckkc.jsourcepad.model;
 
 import kkckkc.jsourcepad.ScopeRoot;
 import kkckkc.jsourcepad.model.bundle.Bundle;
 import kkckkc.jsourcepad.model.bundle.BundleListener;
 import kkckkc.jsourcepad.util.messagebus.AbstractMessageBus;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.syntaxpane.model.LineManager;
 import kkckkc.syntaxpane.model.SourceDocument;
 import kkckkc.syntaxpane.parse.grammar.Language;
 import kkckkc.syntaxpane.parse.grammar.LanguageManager;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 
 import java.io.*;
 
 
 public class DocImpl extends AbstractMessageBus implements Doc, ScopeRoot, BeanFactoryAware {
 	protected LanguageManager languageManager;
 	private DocList docList;
 	protected Window window;
 	protected File backingFile;
 	protected BufferImpl buffer;
 	private TabManager tabManager;
 	private DefaultListableBeanFactory container;
     private SourceDocument sourceDocument;
 
     @Autowired
 	public DocImpl(final Window window, DocList docList, LanguageManager languageManager) {
 		this.docList = docList;
 
         sourceDocument = new SourceDocument();
         this.buffer = new BufferImpl(sourceDocument, this, window);
 		this.buffer.setLanguage(languageManager.getLanguage(null));
 		
 		this.window = window;
 		this.languageManager = languageManager;
 		this.tabManager = new TabManagerImpl(this);
 
         Application.get().topic(BundleListener.class).subscribe(DispatchStrategy.ASYNC, new BundleListener() {
 
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
                         line != null ? line.getCharSequence(false).toString() : "", backingFile);
 		        DocImpl.this.buffer.setLanguage(language);
             }
         });
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
        if (getActiveBuffer() == null) return false;
 		return getActiveBuffer().isModified();
 	}
 
 	@Override
 	public boolean isBackedByFile() {
 		return backingFile != null;
 	}
 
 	@Override
 	public void save() {
 		try {
 			FileWriter fw = new FileWriter(this.backingFile);
 			fw.write(buffer.getText(buffer.getCompleteDocument()));
 			fw.close();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		getActiveBuffer().clearModified();
 		window.topic(Doc.StateListener.class).post().modified(this, true, false);
 	}
 
 	@Override
 	public void saveAs(File file) {
 		try {
 			FileWriter fw = new FileWriter(file);
 			fw.write(buffer.getText(buffer.getCompleteDocument()));
 			fw.close();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		getActiveBuffer().clearModified();
 		
 		this.backingFile = file;
 		
 		window.topic(Project.FileChangeListener.class).post().created(file);
 		window.topic(Doc.StateListener.class).post().modified(this, true, false);
 	}
 
 	@Override
 	public void open(File file) throws IOException {
 		this.backingFile = file;
 
 		BufferedReader br = new BufferedReader(new FileReader(file));
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
