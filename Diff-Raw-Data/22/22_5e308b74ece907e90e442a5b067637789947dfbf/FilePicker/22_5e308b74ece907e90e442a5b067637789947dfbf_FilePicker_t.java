 package com.evervoid.client.ui;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.client.EVClientSaver;
 import com.evervoid.client.KeyboardKey;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class FilePicker extends UIControl implements ClickObserver, ButtonListener, TextInputListener
 {
 	public enum FilePickerMode
 	{
 		LOAD, SAVE;
 		public String getTitle()
 		{
 			switch (this) {
 				case LOAD:
 					return "Load file";
 				case SAVE:
 					return "Save file";
 			}
 			return null;
 		}
 	}
 
 	private static final ColorRGBA sSelectedFileColor = new ColorRGBA(0.95f, 0.95f, 0.95f, 1f);
 	private static final ColorRGBA sUnselectedFileColor = new ColorRGBA(0.8f, 0.8f, 0.95f, 1f);
 	private final ButtonControl aCancelButton;
 	private final TextInputControl aFilenameText;
 	private final Map<StaticTextControl, File> aFiles = new HashMap<StaticTextControl, File>();
 	private final ScrollingControl aFileScrolling;
 	private final Set<FilePickerListener> aListeners = new HashSet<FilePickerListener>();
 	private final PanelControl aMainPanel;
 	private final FilePickerMode aMode;
 	private final ButtonControl aOKButton;
 	private StaticTextControl aSelectedFileText = null;
 
 	public FilePicker(final FilePickerMode mode)
 	{
 		aMode = mode;
 		aMainPanel = new PanelControl(mode.getTitle());
		aFileScrolling = new ScrollingControl(256, 340);
 		for (final File saveFile : EVClientSaver.getAvailableSaveFiles()) {
 			final StaticTextControl text = new StaticTextControl(saveFile.getName(), sUnselectedFileColor);
 			text.registerClickObserver(this);
 			aFiles.put(text, saveFile);
 			aFileScrolling.addUI(text);
 		}
 		aMainPanel.addUI(new DarkBoxControl(aFileScrolling));
 		aMainPanel.addSpacer(1, 16);
 		aFilenameText = new TextInputControl(256);
 		aFilenameText.addTextInputListener(this);
 		aMainPanel.addUI(aFilenameText);
 		aMainPanel.addSpacer(1, 16);
 		final UIControl buttonRow = new UIControl(BoxDirection.HORIZONTAL);
 		buttonRow.addFlexSpacer(1);
 		aCancelButton = new ButtonControl("Cancel");
 		aCancelButton.addButtonListener(this);
 		buttonRow.addUI(aCancelButton);
 		buttonRow.addSpacer(16, 1);
 		aOKButton = new ButtonControl("OK");
 		aOKButton.addButtonListener(this);
 		buttonRow.addUI(aOKButton);
 		aMainPanel.addUI(buttonRow);
 		addUI(new CenteredControl(aMainPanel), 1);
 		aFilenameText.onClick();
 	}
 
 	@Override
 	public void buttonClicked(final UIControl button)
 	{
 		if (button.equals(aCancelButton)) {
 			close();
 		}
 		else if (button.equals(aOKButton)) {
 			confirm();
 		}
 	}
 
 	@Override
 	public boolean click(final Vector2f point)
 	{
 		if (!aMainPanel.inAbsoluteBounds(point)) { // If user clicks outside, cancel
 			close();
 			return false;
 		}
 		return super.click(point);
 	}
 
 	public void close()
 	{
 		for (final FilePickerListener listener : aListeners) {
 			listener.filePickerCanceled(this, aMode);
 		}
 	}
 
 	public void confirm()
 	{
 		String filename = aFilenameText.getText();
 		if (!filename.endsWith(EVClientSaver.sSaveFileExtension)) {
 			filename += EVClientSaver.sSaveFileExtension;
 		}
 		final File selected = new File(EVClientSaver.getSaveFilesDirectory(), filename);
 		for (final FilePickerListener listener : aListeners) {
 			listener.filePicked(this, aMode, selected);
 		}
 	}
 
 	@Override
 	public void onTextInputDefocus(final TextInputControl control)
 	{
 		// Nothing
 	}
 
 	@Override
 	public void onTextInputFocus(final TextInputControl control)
 	{
 		// Nothing
 	}
 
 	@Override
 	public void onTextInputKey(final TextInputControl control, final KeyboardKey key)
 	{
 		String filename = aFilenameText.getText();
 		if (!filename.endsWith(EVClientSaver.sSaveFileExtension)) {
 			filename += EVClientSaver.sSaveFileExtension;
 		}
 		if (aSelectedFileText != null) {
 			aSelectedFileText.setColor(sUnselectedFileColor);
 		}
 		boolean found = false;
 		for (final StaticTextControl text : aFiles.keySet()) {
 			if (text.getText().equalsIgnoreCase(filename)) {
 				found = true;
 				aSelectedFileText = text;
 				aSelectedFileText.setColor(sSelectedFileColor);
 				break;
 			}
 		}
 		if (!found) {
 			aSelectedFileText = null;
 		}
 		if (key.equals(KeyboardKey.ENTER)) {
 			confirm();
 		}
 	}
 
 	public void registerListener(final FilePickerListener listener)
 	{
 		aListeners.add(listener);
 	}
 
 	@Override
 	public void uiClicked(final UIControl clicked)
 	{
 		final File selected = aFiles.get(clicked);
 		if (selected == null) {
 			return;
 		}
 		if (aSelectedFileText != null) {
 			aSelectedFileText.setColor(sUnselectedFileColor);
 		}
 		if (clicked.equals(aSelectedFileText)) { // Clicked twice on the same file
 			confirm();
 		}
 		aSelectedFileText = (StaticTextControl) clicked;
 		aSelectedFileText.setColor(sSelectedFileColor);
 		aFilenameText.setText(aSelectedFileText.getText());
 		aFilenameText.onClick();
 	}
 }
