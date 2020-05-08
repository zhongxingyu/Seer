 package com.t_oster.notenschrank.gui;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import com.t_oster.notenschrank.data.Archive;
 import com.t_oster.notenschrank.data.OCR;
 import com.t_oster.notenschrank.data.OCR.OCRResult;
 import com.t_oster.notenschrank.data.SettingsManager;
 import com.t_oster.notenschrank.data.Sheet;
 import com.t_oster.notenschrank.data.Song;
 import com.t_oster.notenschrank.data.Voice;
 
 public class SortingDialog extends JDialog implements ActionListener{
 	
 	public enum Layout {
 		VERTICAL,
 		HORIZONTAL
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2699371667100327736L;
 	private PreviewPanel previewpanel;
 	private PreviewPanel current;
 	private JPanel mainPanel;
 	private SelectSongBox cbSong;
 	private SelectVoiceBox cbVoice;
 	private JButton bOk;
 	private JButton bAddPage;
 	private JButton bRotatePage;
 	private JButton bGuessSong;
 	private JButton bGuessVoice;
 	private List<Sheet> stackSheets;
 
 	public SortingDialog(JFrame parent, Layout layout){
 		super(parent,"Notenschrank "+SettingsManager.getInstance().getProgramVersion());
 		this.setPreferredSize(new Dimension(800,600));
 		this.previewpanel = new PreviewPanel(this);
 		this.current = new PreviewPanel(this);
 		this.cbSong = new SelectSongBox();
 		this.cbSong.setMaximumSize(new Dimension(500,35));
 		this.cbVoice = new SelectVoiceBox();
 		this.cbVoice.setMaximumSize(new Dimension(500,35));
 		
 		if (OCR.isAvailable()){
 			//Experimental
 			this.bGuessSong = new JButton("raten");
 			this.bGuessSong.addActionListener(this);
 			this.bGuessVoice = new JButton("raten");
 			this.bGuessVoice.addActionListener(this);
 		}
 		
 		this.bOk = new JButton("Speichern");
 		this.bOk.addActionListener(this);
 		this.bAddPage = new JButton("nächste Seite gehört dazu");
 		this.bAddPage.addActionListener(this);
 		this.bRotatePage = new JButton("Seite drehen");
 		this.bRotatePage.addActionListener(this);
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		
 		//ToolTips
 		bOk.setToolTipText("Speichert das aktuelle Blatt im Archiv unter dem angegeben Namen und der angegebenen Stimme");
 		bAddPage.setToolTipText("Fügt das nächste Blatt (linke Seite) zum aktuellen Blatt (rechte Seite) hinzu. Sinnvoll bei mehrseitigen Stücken");
 		bRotatePage.setToolTipText("Dreht das aktuelle Blatt (Sinnvoll wenn falschherum eingescannt)");
 		cbVoice.setToolTipText("Stimme unter der das aktuelle Blatt archiviert werden soll (zB. 1. Klarinette in Bb)");
 		cbSong.setToolTipText("Name unter dem das aktuelle Blatt archiviert werden soll. (zB. Martini)");
 		previewpanel.setToolTipText("Blatt welches als nächstes archiviert wird (Nur angezeigt damit man erkennt ob das aktuelle Blatt mehrseitig ist)");
 		current.setToolTipText("Zeigt die erste Seite des aktuellen Stücks. Zoom mit linker Maustaste in eine der 4 Ecken. Herauszoomen mit rechter Maustaste. Scrollen mit Scrollrad.");
 		
 		this.mainPanel = new JPanel();
 		this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
 		if (layout==Layout.HORIZONTAL){
 			Box b = Box.createHorizontalBox();
 			b.add(Box.createHorizontalGlue());
 			b.add(new JLabel("nächstes Blatt"));
 			b.add(Box.createHorizontalGlue());
 			b.add(new JLabel("aktuelles Blatt"));
 			b.add(Box.createHorizontalGlue());
 			this.mainPanel.add(b);
 			JPanel previewContainer = new JPanel();
 			previewContainer.setLayout(new GridLayout(1,2));
 			previewContainer.add(this.previewpanel);
 			previewContainer.add(this.current);
 			this.mainPanel.add(previewContainer);
 		}
 		else if (layout == Layout.VERTICAL){
 			mainPanel.add(new JLabel("aktuelles Blatt"));
 			mainPanel.add(current);
 			mainPanel.add(new JLabel("nächstes Blatt"));
 			mainPanel.add(this.previewpanel);
 		}
 		Box b=Box.createHorizontalBox();
 		b.add(Box.createHorizontalGlue());
		b.add(new JLabel("Stück:     "));
 		b.add(cbSong);
 		if (bGuessSong!=null){
 			b.add(bGuessSong);
 		}
 		b.add(Box.createHorizontalGlue());
 		this.mainPanel.add(b);
 		b=Box.createHorizontalBox();
 		b.add(Box.createHorizontalGlue());
 		b.add(new JLabel("Stimme:"));
 		b.add(cbVoice);
 		if (bGuessVoice!=null){
 			b.add(bGuessVoice);
 		}
 		b.add(Box.createHorizontalGlue());
 		this.mainPanel.add(b);
 		Box box = Box.createHorizontalBox();
 		box.add(bAddPage);
 		box.add(bRotatePage);
 		box.add(bOk);
 		this.mainPanel.add(box);
 		this.setContentPane(mainPanel);
 		this.pack();
 		this.setLocationRelativeTo(null);
 	}
 	
 	public void showDialog(){
 		
 		try{
 			this.stackSheets = Archive.getInstance().getUnsortedSheets();
 			if (stackSheets.size()==0){
 				JOptionPane.showMessageDialog(null, "Keine Noten zum sortieren vorhanden\nBitte scannen Sie zuerst welche ein", "Nix da.", JOptionPane.OK_OPTION);
 				this.dispose();
 				return;
 			}
 			Sheet cur = stackSheets.remove(0);
 			this.current.showSheet(cur);
 			if (stackSheets.size()>0){
 				Sheet nxt = stackSheets.remove(0);
 				this.previewpanel.showSheet(nxt);
 			}
 			else{
 				bAddPage.setEnabled(false);
 			}
 			this.setModal(true);
 			//bocks invoking Thread until disposed
 			this.setVisible(true);
 		}
 		catch (Exception e){
 			
 		}
 		
 	}
 
 	private void rotateClicked(){
 		try {
 			Sheet s = this.current.getSheet();
 			s.rotateAllPages(1);
 			this.current.refresh();
 		} catch (Exception e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(this, "Fehler beim rotieren.", "Fehler", JOptionPane.OK_OPTION);
 		} 
 	}
 	
 	private void addClicked(){
 		try {
 			Sheet current = this.current.getSheet();
 			Sheet preview = this.previewpanel.getSheet();
 			if (preview != null){
 				current.addPage(this.previewpanel.getSheet());
 				this.current.refresh();
 				preview.delete();
 				if (this.stackSheets.size()>0){
 					this.previewpanel.showSheet(this.stackSheets.remove(0));
 				}
 				else{
 					this.previewpanel.showSheet(null);
 					this.bAddPage.setEnabled(false);
 				}
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			JOptionPane.showMessageDialog(this, "Fehler beim hinzufügen der Seite");
 		}
 	}
 	
 	private void okClicked(){
 		Sheet s = current.getSheet();
 		Song song = cbSong.getSelectedSong();
 		Voice v = cbVoice.getSelectedVoice();
 		if (song==null || v==null){
 			JOptionPane.showMessageDialog(this, "Sie müssen sowohl Titel als auch Stimme eingeben", "Fehler", JOptionPane.OK_OPTION);
 			return;
 		}
 		try {
 			if (Archive.getInstance().contains(song,v)){
 				JPanel existingPreview = new JPanel();
 				existingPreview.setLayout(new BoxLayout(existingPreview, BoxLayout.Y_AXIS));
 				existingPreview.setPreferredSize(new Dimension(400,400));
 				existingPreview.setMinimumSize(new Dimension(400,400));
 				existingPreview.setMaximumSize(new Dimension(400,400));
 				PreviewPanel pp = new PreviewPanel(existingPreview);
 				pp.setPreferredSize(new Dimension(380,380));
 				pp.setSize(380,380);
 				pp.showSheet(Archive.getInstance().getSheet(song,v));
 				existingPreview.add(pp);
 				existingPreview.add(new JLabel( "Datei ist bereits im Archiv."));
 				existingPreview.add(new JLabel ( "Überschreiben?"));
 				if (JOptionPane.showConfirmDialog(this, existingPreview, this.getTitle(), JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION){
 					return;
 				}
 			}
 			Archive.getInstance().addToArchive(s, song, v);
 			s.delete();
 			Sheet p = previewpanel.getSheet();
 			if (p==null){
 				this.dispose();
 			}
 			this.current.showSheet(p);
 			if (this.stackSheets.size()>0){
 				this.previewpanel.showSheet(this.stackSheets.remove(0));
 			}
 			else{
 				this.previewpanel.showSheet(null);
 				this.bAddPage.setEnabled(false);
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			JOptionPane.showMessageDialog(this, "Fehler beim speichern der Seite");
 		}
 	}
 	
 	private void guessSongClicked(){
 		bGuessSong.setEnabled(false);
 		Sheet s = current.getSheet();
 		if (s!=null){
 			try {
 				String[] possible = new String[cbSong.getItemCount()];
 				for (int i=0;i<possible.length;i++){
 					possible[i]=cbSong.getItemAt(i).toString();
 				}
 				OCRResult guess = OCR.findStringInImage(possible, s.getPreview(new Dimension(25,0), new Dimension(50,20), new Dimension(500,200)));
 				if (guess==null){
 					return;
 				}
 				if (guess.matchquality > 70){
 					cbSong.setSelectedIndex(guess.index);
 				}
 				else{
 					cbSong.setSelectedItem(guess.beautified);
 				}
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			
 		}
 		bGuessSong.setEnabled(true);
 	}
 	
 	private void guessVoiceClicked(){
 		bGuessVoice.setEnabled(false);
 		Sheet s = current.getSheet();
 		if (s!=null){
 			try {
 				String[] possible = new String[cbVoice.getItemCount()];
 				for (int i=0;i<possible.length;i++){
 					possible[i]=cbVoice.getItemAt(i).toString();
 				}
 				OCRResult guess1 = OCR.findStringInImage(possible, s.getPreview(new Dimension(0,0), new Dimension(25,20), new Dimension(250,200)));
 				OCRResult guess2 = OCR.findStringInImage(possible, s.getPreview(new Dimension(75,0), new Dimension(25,20), new Dimension(250,200)));
 				OCRResult guess=null;
 				if (guess1==null && guess2!=null){
 					guess=guess2;
 				}
 				else if (guess2==null && guess1!=null){
 					guess=guess1;
 				}
 				else if (guess1==null && guess2 == null){
 					return;
 				}
 				else{
 					guess = guess1.matchquality>guess2.matchquality?guess1:guess2;
 				}
 				if (guess.matchquality > 50){
 					cbVoice.setSelectedIndex(guess.index);
 				}
 				else{
 					cbVoice.setSelectedItem(guess.beautified);
 				}
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			
 		}
 		bGuessVoice.setEnabled(true);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.bRotatePage)){
 			this.rotateClicked();
 		}
 		else if (e.getSource().equals(this.bAddPage)){
 			this.addClicked();
 		}
 		else if (e.getSource().equals(this.bOk)){
 			this.okClicked();	
 		}
 		else if (this.bGuessSong != null && e.getSource().equals(this.bGuessSong)){
 			this.guessSongClicked();
 		}
 		else if (this.bGuessVoice != null && e.getSource().equals(this.bGuessVoice)){
 			this.guessVoiceClicked();
 		}
 	}
 }
