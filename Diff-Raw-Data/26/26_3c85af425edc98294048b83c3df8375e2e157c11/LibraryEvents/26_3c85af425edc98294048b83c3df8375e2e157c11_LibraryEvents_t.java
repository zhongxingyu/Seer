 package MusicStore;
 
 import BackEnd.*;
 import Gui.*;
 import java.awt.Color;
 import java.util.ArrayList;
 import javax.swing.JOptionPane;
 
 public class LibraryEvents implements Gui.EventImplementation, Gui.WindowEvents {
 
 	public GuiObject MainFrame;
 	private ScrollBar LibScroll;
 	private TextButton AccountCredit;
 	private TextButton ManagementButton;
 	private Frame CenterScrollFrame;
 	private User currentUser;
 	private Frame leftPanel;
 	private Frame CreditAdd;
 	private Frame ModifyItem;
 	private Frame Help;
 	private Frame SearchQuery;
 	private Frame RatingWindow;
 	public Item CurrentPlayingItem = null;
 	private int CurrentRatingId;
 
 	/*
 	 * Initialize the library frame.
 	 */
 	public final void MakeElements() {
 		Color ColorScheme = Driver.ColorScheme;
 		MainFrame = new Frame("Library", new DPair(0, 0, 0, 0), new DPair(1, 0, 1, 0), ColorExtension.Lighten(ColorScheme, 1), null);
 		leftPanel = new Frame("LeftPanel", new DPair(0, 0, 0, 0), new DPair(0, 150, 1, 0), Color.WHITE, MainFrame);
 		new Frame("Stripe1", new DPair(0, 0, 0, 0), new DPair(0, 60, 1, 0), ColorExtension.Lighten(ColorScheme, .7), leftPanel);
 		new Frame("Stripe2", new DPair(0, 72, 0, 0), new DPair(0, 6, 1, 0), ColorExtension.Lighten(ColorScheme, .7), leftPanel);
 		new Frame("Stripe3", new DPair(1, -18, 0, 0), new DPair(0, 18, 1, 0), ColorExtension.Lighten(ColorScheme, .7), leftPanel);
 		new TextButton("LibraryButton", new DPair(0, 0, 0, 12), new DPair(1, -18, 0, 36), ColorScheme, leftPanel, "Library", 18);
 		new TextButton("StoreButton", new DPair(0, 0, 0, 54), new DPair(1, -18, 0, 36), ColorScheme, leftPanel, "Store", 18);
 		new TextButton("ViewAlbums", new DPair(0, 18, 0, 96), new DPair(1, -18, 0, 24), ColorScheme, leftPanel, "Albums", 14);
 		new TextButton("ViewAudiobooks", new DPair(0, 18, 0, 126), new DPair(1, -18, 0, 24), ColorScheme, leftPanel, "Audiobooks", 14);
 		new TextButton("ViewFilms", new DPair(0, 18, 0, 156), new DPair(1, -18, 0, 24), ColorScheme, leftPanel, "Films", 14);
 		new TextButton("Search", new DPair(0, 18, 0, 186), new DPair(1, -18, 0, 24), ColorScheme, leftPanel, "Search", 14);
 		ManagementButton = new TextButton("ManagementButton", new DPair(0, 0, 0, 216), new DPair(1, -18, 0, 36), ColorScheme, leftPanel, "Mgmt Tools", 18);
 		AccountCredit = new TextButton("AccountCredit", new DPair(0, 0, 1, -30), new DPair(1, 0, 0, 24), ColorScheme, leftPanel, "Credit: $25.00", 14);
 		new TextButton("Help", new DPair(0, 0, 1, -60), new DPair(1, 0, 0, 24), ColorScheme, leftPanel, "Help", 14);
 		CenterScrollFrame = new Frame("CenterFrame", new DPair(0, leftPanel.GetSize().xOffset + 10, 0, 10), new DPair(1, -leftPanel.GetSize().xOffset - 50, 1, -20), Color.WHITE, MainFrame);
 		LibScroll = new ScrollBar("Scroll", new DPair(1, -30, 0, 10), new DPair(0, 20, 1, -20), ColorScheme, MainFrame, 2000, 200);
 		
 		SearchQuery = new Frame("SearchFrame", new DPair(.5, -100, .5, -50), new DPair(0, 200, 0, 100), ColorScheme, null);
 		new Frame("InsideBorder", new DPair(0, 2, 0, 2), new DPair(1, -4, 1, -4), Color.WHITE, SearchQuery);
 		new TextLabel("RequestLabel", new DPair(0, 4, 0, 4), new DPair(1, -8, 0, 20), Color.WHITE, SearchQuery, "Search for element.", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("SearchText", new DPair(0, 4, 0, 28), new DPair(1, -8, 0, 20), Color.WHITE, SearchQuery, "Search Query", 14, Color.BLACK);
 		new RadioButton("Artist", new DPair(0, 2, 0, 50), new DPair(.5, -3, 0, 20), ColorScheme, SearchQuery, "Search Artist", 12).SetSelected(true);
 		new RadioButton("Title", new DPair(.5, 1, 0, 50), new DPair(.5, -3, 0, 20), ColorScheme, SearchQuery, "Search Title", 12);
 		new TextButton("SearchButton", new DPair(.5, -20, 1, -24), new DPair(0, 40, 0, 20), Driver.ColorScheme, SearchQuery,  "OK", 14);
 		
 		
 		CreditAdd = new Frame("CreditAdd", new DPair(.5, -100, .5, -40), new DPair(0, 200, 0, 80), ColorScheme, null);
 		new Frame("InsideBorder", new DPair(0, 2, 0, 2), new DPair(1, -4, 1, -4), Color.WHITE, CreditAdd);
 		TextLabel Request = new TextLabel("Request", new DPair(0, 4, 0, 4), new DPair(1, -8, 0, 20), Color.WHITE, CreditAdd, "Add credit to account.", 12);
 		Request.SetTextColor(Driver.ColorScheme);
 		new TextBox("Input", new DPair(0, 4, 0, 28), new DPair(1, -8, 0, 20), Color.WHITE, CreditAdd, "Credit", 14, Color.BLACK);
 		new TextButton("CreditAdd", new DPair(.5, -20, 1, -24), new DPair(0, 40, 0, 20), Driver.ColorScheme, CreditAdd,  "OK", 14);
 
 		ModifyItem = new Frame("ModifyItem", new DPair(.5, -200, .5, -114), new DPair(0, 400, 0, 228), ColorScheme, null);
 		new Frame("InsideBorder", new DPair(0, 3, 0, 3), new DPair(1, -6, 1, -6), Color.WHITE, ModifyItem);
 		new TextLabel("Id", new DPair(0, 4, 0, 4), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "ID #000", 12).SetTextColor(Driver.ColorScheme);
 		new TextLabel("Title", new DPair(0, 4, 0, 26), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Title: ", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("TitleInput", new DPair(0, 108, 0, 26), new DPair(1, -115, 0, 18), Color.WHITE, ModifyItem, "newtitle", 12, Driver.ColorScheme);
 		new TextLabel("Artist", new DPair(0, 4, 0, 48), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Artist: ", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("ArtistInput", new DPair(0, 108, 0, 48), new DPair(1, -115, 0, 18), Color.WHITE, ModifyItem, "newartist", 12, Driver.ColorScheme);
 		new TextLabel("Year", new DPair(0, 4, 0, 70), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Year: ", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("YearInput", new DPair(0, 108, 0, 70), new DPair(1, -115, 0, 18), Color.WHITE, ModifyItem, "newyear", 12, Driver.ColorScheme);
 		new TextLabel("Genre", new DPair(0, 4, 0, 92), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Genre: ", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("GenreInput", new DPair(0, 108, 0, 92), new DPair(1, -115, 0, 18), Color.WHITE, ModifyItem, "newgenre", 12, Driver.ColorScheme);
 		new TextLabel("Cost", new DPair(0, 4, 0, 114), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Cost: ", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("CostInput", new DPair(0, 108, 0, 114), new DPair(1, -115, 0, 18), Color.WHITE, ModifyItem, "newcost", 12, Driver.ColorScheme);
 		new TextLabel("Duration", new DPair(0, 4, 0, 136), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Duration: ", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("DurationInputH", new DPair(0, 108, 0, 136), new DPair(0, 40, 0, 18), Color.WHITE, ModifyItem, "newduration", 12, Driver.ColorScheme);
 		new TextBox("DurationInputM", new DPair(0, 152, 0, 136), new DPair(0, 40, 0, 18), Color.WHITE, ModifyItem, "newduration", 12, Driver.ColorScheme);
 		new TextBox("DurationInputS", new DPair(0, 194, 0, 136), new DPair(0, 40, 0, 18), Color.WHITE, ModifyItem, "newduration", 12, Driver.ColorScheme);
 		new TextLabel("Preview", new DPair(0, 4, 0, 158), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Preview: ", 12).SetTextColor(Driver.ColorScheme);
 		new TextBox("PreviewInput", new DPair(0, 108, 0, 158), new DPair(1, -115, 0, 18), Color.WHITE, ModifyItem, "newpreview", 12, Driver.ColorScheme);
 		new TextLabel("Hidden", new DPair(0, 4, 0, 180), new DPair(0, 100, 0, 18), Color.WHITE, ModifyItem, "Hidden: ", 12).SetTextColor(Driver.ColorScheme);
 		new CheckBox("HiddenInput", new DPair(0, 108, 0, 180), new DPair(0, 100, 0, 18), Driver.ColorScheme, ModifyItem, "Hidden", 12);
 		new TextButton("SubmitChanges", new DPair(0, 4, 1, -26), new DPair(0, 140, 0, 20), Driver.ColorScheme, ModifyItem, "Submit Changes", 14);
 		new TextButton("CloseModifyWindow", new DPair(1, -30, 1, -26), new DPair(0, 26, 0, 20), Driver.ColorScheme, ModifyItem, "X", 14);
 		
 		Help = new Frame("HelpFrame", new DPair(0.5, -150, 0.5, -200), new DPair(0, 300, 0, 400), Driver.ColorScheme, null);
 		new Frame("InsideBorder", new DPair(0, 3, 0, 3), new DPair(1, -6, 1, -6), Color.WHITE, Help);
 		String[] info = {
 		"View your personal library by pressing the", "\"Library\" button to the left. View everything", "available in the store by pressing \"Store\",", "or filter your results down to albums,", "audiobooks, or films by pressing the", "respective buttons. Purchase items by",  "pressing the “Buy” button.",
 		"", "Need more credit? Click the button in the", "lower left showing how much credit you", "have."
 		};
 		for (int i = 0; i < info.length; i++) {
 			new TextLabel("label", new DPair(0, 4, 0, i * 24 + 4), new DPair(1, -8, 0, 18), Color.WHITE, Help, info[i], 12).SetTextColor(Driver.ColorScheme);
 		}
 		new TextButton("CloseHelpWindow", new DPair(.7, -4, 1, -26), new DPair(.3, 0, 0, 20), Driver.ColorScheme, Help, "Close", 14);
 		
 		RatingWindow = new Frame("Rating", new DPair(0, 0, 0, 0), new DPair(0, 120, 0, 146), Driver.ColorScheme, null);
 		new TextLabel("Rating", new DPair(0, 3, 0, 3), new DPair(1, -6, 0, 17), Color.WHITE, RatingWindow, "Rating", 12).SetTextColor(Driver.ColorScheme);
 		new TextButton("1star", new DPair(0, 3, 0, 23), new DPair(1, -6, 0, 17), Driver.ColorScheme, RatingWindow, "*", 12);
 		new TextButton("2star", new DPair(0, 3, 0, 43), new DPair(1, -6, 0, 17), Driver.ColorScheme, RatingWindow, "**", 12);
 		new TextButton("3star", new DPair(0, 3, 0, 63), new DPair(1, -6, 0, 17), Driver.ColorScheme, RatingWindow, "***", 12);
 		new TextButton("4star", new DPair(0, 3, 0, 83), new DPair(1, -6, 0, 17), Driver.ColorScheme, RatingWindow, "****", 12);
 		new TextButton("5star", new DPair(0, 3, 0, 103), new DPair(1, -6, 0, 17), Driver.ColorScheme, RatingWindow, "*****", 12);
 		new TextButton("0star", new DPair(0, 3, 0, 123), new DPair(1, -6, 0, 17), Driver.ColorScheme, RatingWindow, "n/a", 12);
 	}
 
 	/*
 	 * Set the data elements for the edit-item frame.
 	 */
 	private int CurrentModifyWindowId = 0;
 	private void SetModify(int itemIndex) {
 		Item current = DataLoader.getItemById(itemIndex);
 		if (current != null) {
 			CurrentModifyWindowId = itemIndex;
 			ModifyItem.SetParent(MainFrame);
 			Driver.GetGuiMain().GetTextBoxes();
 			((TextLabel) ModifyItem.GetChild("Id")).SetText("ID #" + itemIndex);
 			((TextBox) ModifyItem.GetChild("TitleInput")).SetText(current.getName());
 			((TextBox) ModifyItem.GetChild("ArtistInput")).SetText(current.getCreator());
 			((TextBox) ModifyItem.GetChild("YearInput")).SetText("" + current.getYearOfRelease());
 			((TextBox) ModifyItem.GetChild("GenreInput")).SetText(current.getGenre());
 			((TextBox) ModifyItem.GetChild("CostInput")).SetText("" + current.getPrice());
 			((TextBox) ModifyItem.GetChild("DurationInputH")).SetText("" + current.getHour());
 			((TextBox) ModifyItem.GetChild("DurationInputM")).SetText("" + current.getMinute());
 			((TextBox) ModifyItem.GetChild("DurationInputS")).SetText("" + current.getSecond());
 			((TextBox) ModifyItem.GetChild("PreviewInput")).SetText("" + current.getPreview());
 			((CheckBox) ModifyItem.GetChild("HiddenInput")).SetSelected(current.isVisible());
 			((CheckBox) ModifyItem.GetChild("HiddenInput")).SetText(((CheckBox) ModifyItem.GetChild("HiddenInput")).GetSelected() ? "true" : "false");
 		}
 	}
 	
 
 	private int CurrentList = 1; // 0 means user's library, 1 means albums, 2 means audiobooks, 3 means films
 	private int PixelOffset = 0; // How many pixels offset the 
 	private int ViewIndex = 0; // determines which index represents the top most box in the scroll list
 	private ArrayList<Frame> ScrollList = new ArrayList();
 
 	private final int LIST_ELEMENT_HEIGHT = 55;
 	private final int LIST_ELEMENT_SPACING = 5;
 	
 	/*
 	 * Add a list element to the scrolling list.
 	 */
 	private void AddListElement() {
 		// ScrollList should never exceed the number of elements in the current list.
 		Frame newFrame = new Frame("ScrollList" + ScrollList.size(), new DPair(0, 0, 0, 0), new DPair(1, 0, 0, LIST_ELEMENT_HEIGHT), Driver.ColorScheme, CenterScrollFrame);
 		new TextLabel("TopText", new DPair(0, 3, 0, 3), new DPair(1, -58, .5, -3), ColorExtension.Lighten(Driver.ColorScheme, .15), newFrame, "My Text", 14);
 		new TextLabel("BottomText", new DPair(0, 0, .5, 0), new DPair(1, -52, .5, 0), Driver.ColorScheme, newFrame, "My Text", 14);
 		new Frame("Divider", new DPair(1, -52, 0, 2), new DPair(0, 2, 1, -4), Color.WHITE, newFrame);
 		new TextButton("Action", new DPair(1, -47, 0, 3), new DPair(0, 44, 1, -6), Driver.ColorScheme, newFrame, "Buy", 14);
 		new TextButton("Action2", new DPair(1, -94, 0, 3), new DPair(0, 44, 1, -6), Driver.ColorScheme, newFrame, "Edit", 14).SetVisible(false);
 		ScrollList.add(newFrame);
 	}
 
 	/*
 	 * Set the text of a single list element.
 	 */
 	private void SetListText(TextLabel rowOne, TextLabel rowTwo, Item currentItem) {
 		if (currentItem instanceof Album) {
 			rowOne.SetText(String.format("Artist: %s; Album: %s", currentItem.getCreator(), currentItem.getName()));
 			rowTwo.SetText(String.format("Genre: %s; Year: %d; Rating: %.2f; Cost: %.2f", currentItem.getGenre(), currentItem.getYearOfRelease(), currentItem.getRating(), currentItem.getPrice()));
 		} else if (currentItem instanceof Film) {
 			rowOne.SetText(String.format("Title: %s; Producer: %s", currentItem.getName(), currentItem.getCreator()));
 			rowTwo.SetText(String.format("Genre: %s; Year: %d; Rating: %.2f; Cost: %.2f", currentItem.getGenre(), currentItem.getYearOfRelease(), currentItem.getRating(), currentItem.getPrice()));
 		} else if (currentItem instanceof Audiobook) {
 			rowOne.SetText(String.format("Title: %s; Author: %s", currentItem.getName(), currentItem.getCreator(), currentItem.getRating()));
 			rowTwo.SetText(String.format("Genre: %s; Year: %d; Rating: %.2f; Cost: %.2f", currentItem.getGenre(), currentItem.getYearOfRelease(), currentItem.getRating(), currentItem.getPrice()));
 		}
 	}
 	
 	/*
 	 * Get the item ID that corresponds to a list frame.
 	 */
 	private int GetItemIdOfFrame(Frame frame) {
 		PixelOffset = (int) LibScroll.GetValue();
 		ViewIndex = (int) Math.floor((double) PixelOffset / (LIST_ELEMENT_HEIGHT + LIST_ELEMENT_SPACING));
 		int id = ViewIndex + ScrollList.indexOf(frame);
 		if (CurrentList == 0) {
 			return (int) ActiveList.get(id);
		} else if (CurrentList >= 1 && CurrentList <= 5) {
 			return (int) ((Item) ActiveList.get(id)).getId();
 		}
 		return 0;
 	}
 
 	private ArrayList ActiveList = new ArrayList();
 	private int LastCurrentListValue = 0;
 	/*
 	 * Update the scrolling frames.
 	 */
 	private void SetFrames() {
 		AccountCredit.SetText(String.format("Credit: $%.2f", Driver.CurrentUser.getCredit()));
 		Frame current;
 		TextLabel rowOne;
 		TextLabel rowTwo;
 		TextButton commandButton;
 		if (LastCurrentListValue != CurrentList) {
 			ArrayList temporaryList = null;
 			if (CurrentList != 5) {ActiveList.clear();}
 			if (CurrentList == 0) {temporaryList = Driver.CurrentUser.getPurchaseHistory();}
 			else if (CurrentList == 1) {temporaryList = DataLoader.getAlbums();}
 			else if (CurrentList == 2) {temporaryList = DataLoader.getAudiobooks();}
 			else if (CurrentList == 3) {temporaryList = DataLoader.getFilm();}
 			if (CurrentList >= 1 && CurrentList <= 3) {
 				// if the use is looking at a store page, weed out all invisible songs/albums *unless* it is a administrator.
 				for (int i = 0; i < temporaryList.size(); i++) {
 					if (temporaryList.get(i) instanceof Item && (!((Item) temporaryList.get(i)).isVisible() || Driver.CurrentUser.getAdministrator())) {
 						ActiveList.add(temporaryList.get(i));
 					}
 				}
 			} else if (CurrentList == 0) {
 				for (int i = 0; i < temporaryList.size(); i++) {
 					ActiveList.add(temporaryList.get(i));
 				}
 			} else if (CurrentList == 4) {
 				temporaryList = DataLoader.getAlbums();
 				for (int i = 0; i < temporaryList.size(); i++) {
 					if (temporaryList.get(i) instanceof Item && (!((Item) temporaryList.get(i)).isVisible() || Driver.CurrentUser.getAdministrator())) {
 						ActiveList.add(temporaryList.get(i));
 					}
 				}
 				
 				temporaryList = DataLoader.getAudiobooks();
 				for (int i = 0; i < temporaryList.size(); i++) {
 					if (temporaryList.get(i) instanceof Item && (!((Item) temporaryList.get(i)).isVisible() || Driver.CurrentUser.getAdministrator())) {
 						ActiveList.add(temporaryList.get(i));
 					}
 				}
 				
 				temporaryList = DataLoader.getFilm();
 				for (int i = 0; i < temporaryList.size(); i++) {
 					if (temporaryList.get(i) instanceof Item && (!((Item) temporaryList.get(i)).isVisible() || Driver.CurrentUser.getAdministrator())) {
 						ActiveList.add(temporaryList.get(i));
 					}
 				}
 			}
 			LastCurrentListValue = CurrentList;
 		}
 		// this should never happen, but just in case it does, break the function before an error is thrown.
 		if (ActiveList == null) {
 			return;
 		}
 		int MaximumIndex = ActiveList.size();
 		LibScroll.SetMax(Math.max(0, MaximumIndex * (LIST_ELEMENT_HEIGHT + LIST_ELEMENT_SPACING) - Driver.GetGuiMain().GetWindow().getHeight() + 50));
 		PixelOffset = (int) LibScroll.GetValue();
 		ViewIndex = (int) Math.floor((double) PixelOffset / (LIST_ELEMENT_HEIGHT + LIST_ELEMENT_SPACING));
 		for (int i = 0; i < ScrollList.size(); i++) {
 			// Position the frame appropriately.
 			current = ScrollList.get(i);
 			current.GetPosition().yOffset = i * (LIST_ELEMENT_HEIGHT + LIST_ELEMENT_SPACING) - PixelOffset % (LIST_ELEMENT_HEIGHT + LIST_ELEMENT_SPACING);
 			rowOne = (TextLabel) current.GetChild("TopText");
 			rowTwo = (TextLabel) current.GetChild("BottomText");
 			commandButton = (TextButton) current.GetChild("Action");
 
 			// If the index we're looking for is out of bounds, don't display the frame.
 			if (i + ViewIndex >= MaximumIndex) {
 				CenterScrollFrame.RemoveChild(current);
 			} else {
 				CenterScrollFrame.AddChild(current);
 				// Fill the text labels with content based on what view option is selected.
 				if (CurrentList == 0) {
 					if (!current.GetChild("Action2").GetVisible() && !Driver.CurrentUser.getAdministrator()) {
 						current.GetChild("Divider").GetPosition().xOffset = -99;
 						current.GetChild("TopText").GetSize().xOffset = -105;
 						current.GetChild("BottomText").SetSize(current.GetChild("TopText").GetSize());
 						current.GetChild("Action2").SetVisible(true);
 						((TextButton) current.GetChild("Action2")).SetText("Rate");
 						Driver.GetGuiMain().GetTextBoxes();
 					}
 					Item currentItem = DataLoader.getItemById(((Integer) ActiveList.get(i + ViewIndex)).intValue());
 					SetListText(rowOne, rowTwo, currentItem);
 					if (CurrentPlayingItem == currentItem) {
 						commandButton.SetText("Stop");
 					} else {
 						commandButton.SetText("Play");
 					}
 				} else if (CurrentList >= 1 && CurrentList <= 5) {
 					if (current.GetChild("Action2").GetVisible() && !Driver.CurrentUser.getAdministrator()) {
 						current.GetChild("Divider").GetPosition().xOffset = -52;
 						current.GetChild("TopText").GetSize().xOffset = -58;
 						current.GetChild("BottomText").SetSize(current.GetChild("TopText").GetSize());
 						current.GetChild("Action2").SetVisible(false);
 					}
 					Item currentItem = (Item) ActiveList.get(i + ViewIndex);
 					SetListText(rowOne, rowTwo, currentItem);
 					if (currentItem.isVisible()) {rowTwo.SetText(rowTwo.GetText() + "; HIDDEN");}
 					if (Driver.CurrentUser.getPurchaseHistory().contains(currentItem.getId())) {
 						if (CurrentPlayingItem == currentItem) {
 							commandButton.SetText("Stop");
 						} else {
 							commandButton.SetText("Play");
 						}
 					} else {
 						commandButton.SetText("Buy");
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * Constructor. Create the frame and set all the scrolling frames, too.
 	 */
 	public LibraryEvents() {
 		int WindowHeight = Driver.GetGuiMain().GetWindow().getSize().height;
 		MakeElements();
 		int NumberOfFrames = (int) ((double) WindowHeight / (LIST_ELEMENT_HEIGHT));
 //		System.out.println("Height of window: " + WindowHeight);
 		for (int i = 0; i < 18; i++) { // number of scrolling frames is determined by height of window or number of albums to display.
 			AddListElement();
 		}
 	}
 
 	/**
 	 * Fires when any button in the frame is clicked.
 	 * @param button The button.
 	 * @param x The x location of the mouse (relative to the screen)
 	 * @param y Same but for y.
 	 */
 	@Override
 	public void ButtonClicked(GuiObject button, int x, int y) {
//		System.out.println("Button Clicked: " + button.GetName());
 		switch (button.GetName()) {
 			case "LibraryButton":
 				CurrentList = 0;
 				break;
 			case "StoreButton":
 				CurrentList = 4;
 				break;
 			case "ViewAlbums":
 				CurrentList = 1;
 				break;
 			case "ViewAudiobooks":
 				CurrentList = 2;
 				break;
 			case "ViewFilms":
 				CurrentList = 3;
 				break;
 			case "Search":
 				MainFrame.AddChild(SearchQuery);
 				MainFrame.RemoveChild(CreditAdd);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "ManagementButton":
 				currentUser = Driver.CurrentUser;
 				if (currentUser.getAdministrator()) {
 					Driver.SetFrame("Management");
 				} else {
 					JOptionPane.showMessageDialog(null, "Access denied - Admin only", "", JOptionPane.WARNING_MESSAGE);
 				}
 				break;
 			case "Action":
//				System.out.println("Buy or Play button Pressed.");
 				// "Buy" or "Play" button pressed.
				if (button instanceof TextButton && ((TextButton) button).GetText().equals("Buy") && CurrentList >= 1 && CurrentList <= 5) {
 					int id = GetItemIdOfFrame((Frame) button.GetParent());
//					System.out.println("Pressed buy for item: " + id);
 					boolean success = Driver.CurrentUser.purchaseItem(id);
 					if (success) {
 						((TextButton) button).SetText("Play");
 					} else {
 						System.out.println("Buy failed.");
 					}
 				} else if (button instanceof TextButton && ((TextButton) button).GetText().equals("Play") && CurrentList == 0) {
 					int id = GetItemIdOfFrame((Frame) button.GetParent());
 					if (CurrentPlayingItem != null) {CurrentPlayingItem.stopAudio();}
 					CurrentPlayingItem = DataLoader.getItemById(id);
 					CurrentPlayingItem.playAudioFull();
 				} else if (button instanceof TextButton && ((TextButton) button).GetText().equals("Stop") && CurrentPlayingItem != null) {
 					CurrentPlayingItem.stopAudio();
 					CurrentPlayingItem = null;
				} else if (button instanceof TextButton && ((TextButton) button).GetText().equals("Play") && CurrentList >= 1 && CurrentList <= 5) {
 					int id = GetItemIdOfFrame((Frame) button.GetParent());
 					CurrentPlayingItem = DataLoader.getItemById(id);
 					CurrentPlayingItem.playAudioPreview();
 				}
 				AccountCredit.SetText(String.format("Credit: $%.2f", Driver.CurrentUser.getCredit()));
 				break;
 			case "AccountCredit":
 				MainFrame.AddChild(CreditAdd);
 				MainFrame.RemoveChild(SearchQuery);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "CreditAdd":
 				double amount = 0;
 				try {
 					amount = Double.parseDouble(((TextBox) CreditAdd.GetChild("Input")).GetText());
 				} catch(Exception e) {}
 				Driver.CurrentUser.grantCredit(amount);
 				MainFrame.RemoveChild(CreditAdd);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "Input":
 				if (button instanceof TextBox && ((TextBox) button).GetText().equals("Credit")) {
 					((TextBox) button).SetText("");
 				}
 				break;
 			case "SearchButton":
 				String SearchText = ((TextBox) SearchQuery.GetChild("SearchText")).GetText();
 				CurrentList = 5;
 				ArrayList<Item> temporaryList;
 				if (((RadioButton) SearchQuery.GetChild("Artist")).GetSelected()) {
 					temporaryList = DataLoader.searchForItemArtist(SearchText);
 				} else {
 					temporaryList = DataLoader.searchForItemTitle(SearchText);
 				}
 				ActiveList.clear();
 				for (Item i : temporaryList) {
 					ActiveList.add(i);
 				}
 				MainFrame.RemoveChild(SearchQuery);
 				break;
 			case "SearchText":
 				if (button instanceof TextBox && ((TextBox) button).GetText().equals("Search Query")) {
 					((TextBox) button).SetText("");
 				}
 				break;
 			case "Action2":
 				if (button instanceof TextButton && ((TextButton) button).GetText().equals("Edit") && CurrentList >= 0 && CurrentList <= 4) {
 					int id = GetItemIdOfFrame((Frame) button.GetParent());
 					SetModify(id);
 				} else if (button instanceof TextButton && ((TextButton) button).GetText().equals("Rate") && CurrentList == 0) {
 					CurrentRatingId = GetItemIdOfFrame((Frame) button.GetParent());
 					RatingWindow.SetParent(MainFrame);
 					RatingWindow.SetPosition(new DPair(0, x - RatingWindow.GetSize().xOffset, 0, y));
 					Driver.GetGuiMain().GetTextBoxes();
 				}
 				break;
 			case "HiddenInput":
 				((CheckBox) button).SetText(((CheckBox) button).GetSelected() ? "true" : "false");
 				break;
 			case "SubmitChanges":
 				Item current = DataLoader.getItemById(CurrentModifyWindowId);
 				if (current != null) {
 					current.setCreator(((TextBox) ModifyItem.GetChild("ArtistInput")).GetText());
 					current.setName(((TextBox) ModifyItem.GetChild("TitleInput")).GetText());
 					try {	current.setYearOfRelease(Integer.parseInt(((TextBox) ModifyItem.GetChild("YearInput")).GetText()));} catch (Exception e) {}
 					current.setGenre(((TextBox) ModifyItem.GetChild("GenreInput")).GetText());
 					try {	current.setPrice(Double.parseDouble(((TextBox) ModifyItem.GetChild("CostInput")).GetText()));} catch (Exception e) {}
 					int d = 0;
 					try {
 						d += Integer.parseInt(((TextBox) ModifyItem.GetChild("DurationInputH")).GetText()) * 3600;
 					} catch(Exception e) {}
 					try {
 						d += Integer.parseInt(((TextBox) ModifyItem.GetChild("DurationInputM")).GetText()) * 60;
 					} catch(Exception e) {}
 					try {
 						d += Integer.parseInt(((TextBox) ModifyItem.GetChild("DurationInputS")).GetText());
 					} catch(Exception e) {}
 					current.setDuration(d);
 					current.setPreview(((TextBox) ModifyItem.GetChild("PreviewInput")).GetText());
 					current.setHidden(((CheckBox) ModifyItem.GetChild("HiddenInput")).GetSelected());
 				}
 				ModifyItem.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "CloseModifyWindow":
 				ModifyItem.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "CloseHelpWindow":
 				Help.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "Help":
 				Help.SetParent(MainFrame);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "1star":
//				System.out.println("CurrentRatingId: " + CurrentRatingId);
 				Driver.CurrentUser.rateItem(CurrentRatingId, 1);
 				RatingWindow.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "2star":
 				Driver.CurrentUser.rateItem(CurrentRatingId, 2);
 				RatingWindow.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "3star":
 				Driver.CurrentUser.rateItem(CurrentRatingId, 3);
 				RatingWindow.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "4star":
 				Driver.CurrentUser.rateItem(CurrentRatingId, 4);
 				RatingWindow.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "5star":
 				Driver.CurrentUser.rateItem(CurrentRatingId, 5);
 				RatingWindow.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 			case "0star":
 				Driver.CurrentUser.rateItem(CurrentRatingId, 0);
 				RatingWindow.SetParent(null);
 				Driver.GetGuiMain().GetTextBoxes();
 				break;
 		}
 		SetFrames();
 		if (!(button instanceof TextBox)) {Driver.GetGuiMain().GetTextBoxes();}
 	}
 
 	/**
 	 * Fires when the window appears.
 	 */
 	@Override public void onWindowShown() {
 		if (!Driver.CurrentUser.getAdministrator()) {
 			ManagementButton.SetParent(null);
 		} else {
 			ManagementButton.SetParent(leftPanel);
 			for (Frame i : ScrollList) {
 				i.GetChild("Divider").SetPosition(new DPair(1, -99, 0, 2));
 				i.GetChild("TopText").SetSize(new DPair(1, -105, .5, -3));
 				i.GetChild("BottomText").SetSize(i.GetChild("TopText").GetSize());
 				i.GetChild("Action2").SetVisible(true);
 			}
 		}
 		AccountCredit.SetText(String.format("Credit: $%.2f", Driver.CurrentUser.getCredit()));
 		LastCurrentListValue = -1;
 		SetFrames();
 	}
 	@Override public void onWindowHide() {	}
 	/**
 	 * Whenever the window resizes, function will change the frames that are visible.
 	 */
 	@Override public void onWindowResize() {
 		SetFrames();
 	}
 	
 	@Override
 	public void MouseDown(GuiObject button, int x, int y) {}
 
 	@Override
 	public void MouseUp(GuiObject button, int x, int y) {}
 
 	/**
 	 * Every time the mouse moves, change the visible frames in the scroll list.
 	 * @param button The button over which the mouse is.
 	 * @param x THe x location of the mouse on the screen.
 	 * @param y Same but for y.
 	 */
 	@Override
 	public void MouseMove(GuiObject button, int x, int y) {
 		SetFrames();
 	}
 }
