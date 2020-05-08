 package net.mytcg.topcar.ui;
 
 import net.mytcg.topcar.ui.custom.ColorLabelField;
 import net.mytcg.topcar.ui.custom.FixedButtonField;
 import net.mytcg.topcar.ui.custom.SexyEditField;
 import net.mytcg.topcar.ui.custom.ThumbnailField;
 import net.mytcg.topcar.util.Card;
 import net.mytcg.topcar.util.Const;
 import net.mytcg.topcar.util.SettingsBean;
 import net.rim.device.api.io.Base64InputStream;
 import net.rim.device.api.io.Base64OutputStream;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 
 public class NoteScreen extends AppScreen implements FieldChangeListener
 {
 	FixedButtonField exit = new FixedButtonField(Const.back);
 	FixedButtonField save = new FixedButtonField(Const.save);
	SexyEditField note = new SexyEditField(Const.getWidth(),Const.getUsableHeight()-90);
 	Card card = null;
 	
 	public NoteScreen(Card card, AppScreen screen)
 	{
 		super(screen);
 		add(new ThumbnailField(card));
 		add(new ColorLabelField(Const.notes));
 		this.card = card;
 		if (card.getNote().length() >= 0) {
 			String nt = "";
 			try {
 				nt = new String(Base64InputStream.decode(card.getNote()));
 			} catch (Exception e) {
 				nt = "";
 			}
 			note.setText(nt);
 		}
 		add(note);
 		
 		bgManager.setStatusHeight(exit.getContentHeight());
 		
 		exit.setChangeListener(this);
 		save.setChangeListener(this);
 		
 		addButton(save);
 		addButton(new FixedButtonField(""));
 		addButton(exit);
 	}
 	
 	public void fieldChanged(Field f, int i) {
 		if (f == exit) {
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 		} else if (f == save) {
 			String lbl="";
 			try {
 				lbl = new String(Base64OutputStream.encode(note.getText().getBytes(), 0, note.getText().length(), false, false), "UTF-8");
 			} catch (Exception e) {
 				lbl="";
 			}
 			SettingsBean _instance = SettingsBean.getSettings();
 			card.setNote(lbl);
 			_instance.setImages(card.getId(), card);
 			SettingsBean.saveSettings(_instance);
 			
 			doConnect(Const.savenote+lbl+Const.cardid+card.getId());
 		}
 	}
 }
