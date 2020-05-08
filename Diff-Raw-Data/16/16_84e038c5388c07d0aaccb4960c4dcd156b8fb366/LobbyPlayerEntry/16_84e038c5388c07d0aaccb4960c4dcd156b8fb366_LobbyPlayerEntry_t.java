 package com.evervoid.client.views.lobby;
 
 import com.evervoid.client.ui.CheckboxControl;
 import com.evervoid.client.ui.CheckboxListener;
 import com.evervoid.client.ui.ImageControl;
 import com.evervoid.client.ui.MarginSpacer;
 import com.evervoid.client.ui.RowControl;
 import com.evervoid.client.ui.StaticTextControl;
 import com.evervoid.network.lobby.LobbyPlayer;
 import com.evervoid.state.data.GameData;
 import com.jme3.math.ColorRGBA;
 
 public class LobbyPlayerEntry extends MarginSpacer implements CheckboxListener
 {
 	private static String sPlayerFontName = "redensek";
 	private static int sPlayerFontSize = 24;
 	private static ColorRGBA sPlayerNameColor = new ColorRGBA(0.05f, 0.05f, 0.15f, 1f);
 	private boolean aIsSelf;
 	private final LobbyView aLobbyView;
 	private final ImageControl aPlayerColorIcon;
 	private final StaticTextControl aPlayerName;
 	private final ImageControl aRaceIcon;
 	private final StaticTextControl aRaceName;
 	private final CheckboxControl aReadyCheckbox;
 	private final RowControl aRow;
 
 	LobbyPlayerEntry(final LobbyView view)
 	{
 		super(4, 4, 0, 0, new RowControl());
 		aLobbyView = view;
 		aRow = (RowControl) aContained;
 		aPlayerName = new StaticTextControl("", sPlayerNameColor, sPlayerFontName, sPlayerFontSize);
 		aReadyCheckbox = new CheckboxControl("icons/icon_ready.png", "icons/icon_ready_not.png");
 		aReadyCheckbox.addListener(this);
 		addUI(aReadyCheckbox);
 		aPlayerColorIcon = new ImageControl();
 		addUI(aPlayerColorIcon);
 		addUI(aPlayerName);
 		addFlexSpacer(1);
 		aRaceIcon = new ImageControl();
 		aRaceName = new StaticTextControl("", ColorRGBA.Black, "redensek", 24);
 		addUI(aRaceIcon);
 		addUI(aRaceName);
 		smoothAppear(0.4f);
 		aIsSelf = false;
 	}
 
 	@Override
 	public void checkboxChecked(final CheckboxControl checkbox, final boolean checked)
 	{
 		if (aIsSelf) {
 			aLobbyView.setPlayerReady(checked);
 		}
 	}
 
 	void removeEntry()
 	{
		smoothDisappear(0.4f, new Runnable()
		{
			@Override
			public void run()
			{
				deleteUI();
			}
		});
 	}
 
 	/**
 	 * Updates this lobby player row
 	 * 
 	 * @param player
 	 *            The LobbyPlayer held by this row
 	 * @param isSelf
 	 *            True if this row is the local client
 	 * @param gamedata
 	 *            The game data
 	 * @param isFirst
 	 *            True if this row is the first of the whole table
 	 * @param isLast
 	 *            True if this row is the last of the whole table (not mutually exclusive with isFirst if there's only one row)
 	 */
 	void updateEntry(final LobbyPlayer player, final boolean isSelf, final GameData gamedata, final boolean isFirst,
 			final boolean isLast)
 	{
 		aIsSelf = isSelf;
 		aRow.updateRowSprites(isFirst, isLast);
 		if (isSelf) {
 			aReadyCheckbox.setSprites("icons/icon_ready.png", "icons/icon_ready_not.png");
 		}
 		else {
 			aReadyCheckbox.setSprites("icons/icon_ready_noarrow.png", "icons/icon_ready_not_noarrow.png");
 		}
 		aReadyCheckbox.setChecked(player.isReady());
 		aReadyCheckbox.setCheckable(isSelf);
 		aPlayerName.setText(player.getNickname(), false);
 		aPlayerColorIcon.setSprite("icons/colors/" + player.getColorName() + ".png");
 		aRaceIcon.setSprite(gamedata.getRaceData(player.getRace()).getRaceIcon("medium_black"));
 		aRaceName.setText(gamedata.getRaceData(player.getRace()).getTitle(), false);
 	}
 }
