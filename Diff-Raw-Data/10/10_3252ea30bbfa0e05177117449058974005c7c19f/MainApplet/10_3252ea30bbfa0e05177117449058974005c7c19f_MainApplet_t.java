 package org.libreSubsApplet;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Locale;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JApplet;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.libreSubsApplet.dropFile.DropFilesTarget;
 import org.libreSubsApplet.utils.LocaleUtil;
 import org.libreSubsApplet.utils.SubtitleResourceResolver;
 
 @SuppressWarnings("serial")
 public class MainApplet extends JApplet implements OutputListener{
 
 	private JTextArea stringBucketLabel;
 	
 	@Override
 	public void init() {
 		setLookAndfeel();
 
 		setLayout(new BorderLayout());
 		final DroppedFilesProcessor dropFileListener = createDroppedFileProcessor();
 		final JComboBox languageChooser = createLanguageChooser(dropFileListener);
 		add(languageChooser,BorderLayout.PAGE_START);
 		createDropFileTextArea(dropFileListener);
 		printIntroductionText();
 	}
 
 	private void setLookAndfeel() {
 		try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (final ClassNotFoundException e) {
             e.printStackTrace();
         } catch (final InstantiationException e) {
             e.printStackTrace();
         } catch (final IllegalAccessException e) {
             e.printStackTrace();
         } catch (final UnsupportedLookAndFeelException e) {
             e.printStackTrace();
         }
 
         SwingUtilities.updateComponentTreeUI(this);
 	}
 
 	private void printIntroductionText() {
 		info("Para pegar a legenda arraste o arquivo de vídeo aqui.");
 		info("A legenda vai ser salva junto do arquivo de vídeo.");
 		info("Para fazer upload de uma legenda que ainda não existe,");
 		info("arraste o arquivo de vídeo e o arquivo srt aqui.");
 		info("O arquivo .srt deve ter o mesmo nome do arquivo de vídeo.");
 		info("Se você arrastar um diretório ele será escaneado, fazendo upload das legendas que tiverem um arquivo");
 		info("com o mesmo nome e download para os arquivos que não tiverem legenda.");
 	}
 
 	private JComboBox createLanguageChooser(
 			final DroppedFilesProcessor dropFileListener) {
 		
 		final JComboBox languageChooser = new JComboBox(LocaleUtil.getSortedAvailableLocales());
 		languageChooser.setRenderer(new DefaultListCellRenderer(){
 			@Override
 			public Component getListCellRendererComponent(final JList list,
 					final Object value, final int index, final boolean isSelected,
 					final boolean cellHasFocus) {
 				final Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 				setText(((Locale)value).getDisplayName());
 				return listCellRendererComponent;
 			}
 		});
 		languageChooser.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				final Locale language = (Locale)languageChooser.getSelectedItem();
 				dropFileListener.setLanguage(language.toString());
 			}
 		});
 		
 		languageChooser.setSelectedItem(Locale.getDefault());
 		
 		return languageChooser;
 	}
 
 	private DroppedFilesProcessor createDroppedFileProcessor() {
 		final SubtitleResourceResolver srtSource = new SubtitleResourceResolver(getDownloadUrl(), getUploadUrl());
 		final DroppedFilesProcessor dropFileListener = new DroppedFilesProcessor(srtSource, this, Locale.getDefault().toString());
 		return dropFileListener;
 	}
 
 	private void createDropFileTextArea(final DroppedFilesProcessor dropFileListener) {
 		stringBucketLabel = new SubtitleDropTextArea();
 		final DropFilesTarget dropFilesTarget = new DropFilesTarget();
 		dropFilesTarget.addDropFileListener(dropFileListener);
 		stringBucketLabel.setDropTarget(dropFilesTarget);
 		add(stringBucketLabel,BorderLayout.CENTER);
 	}
 
     public String getDownloadUrl() {
         final String srtProviderURL = getParameter("download");
 		
 		if(srtProviderURL==null){
 			return "http://127.0.0.1:8081/download?id=%id&lang=%lang";
 		}
         return srtProviderURL;
     }
     
     public String getUploadUrl() {
         final String srtProviderURL = getParameter("upload");
 		
 		if(srtProviderURL==null){
 			return "http://127.0.0.1:8081/upload";
 		}
         return srtProviderURL;
     }
 
 	@Override
 	public void info(final String info) {
 		stringBucketLabel.append(info+"\n");
 		scrollDown();
 	}
 
 	private void scrollDown() {
 		stringBucketLabel.setCaretPosition(stringBucketLabel.getDocument().getLength());
 	}
 
 	@Override
 	public void error(final String error) {
 		stringBucketLabel.append(error+"\n");
 		scrollDown();
 	}
 }
