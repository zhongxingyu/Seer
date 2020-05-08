 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components.menubar;
 
 import com.dmdirc.ServerState;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.components.IconManager;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.addons.ui_swing.dialogs.channellist.ChannelListDialog;
 import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
 import com.dmdirc.addons.ui_swing.injection.DialogProvider;
 import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
 import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.GroupChat;
 import com.dmdirc.interfaces.WindowModel;
 import com.dmdirc.parser.common.ChannelJoinRequest;
 
 import java.awt.Dialog;
 import java.util.Optional;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 
 /**
  * A menu to provide channel related commands in the menu bar.
  */
 @Singleton
 public class ChannelMenu extends JMenu implements MenuListener {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Icon manager. */
     private final IconManager iconManager;
     /** Main frame. */
     private final MainFrame mainFrame;
     /** Dialog provider. */
     private final KeyedDialogProvider<GroupChat, ChannelSettingsDialog> dialogProvider;
     /** Channel list dialog provider. */
     private final DialogProvider<ChannelListDialog> channelListDialogProvider;
     /** Active frame manager. */
     private final ActiveFrameManager activeFrameManager;
     /** Menu items to be disabled/enabled. */
     private JMenuItem csd;
     private JMenuItem join;
     private JMenuItem list;
 
     @Inject
     public ChannelMenu(
             final ActiveFrameManager activeFrameManager,
             final KeyedDialogProvider<GroupChat, ChannelSettingsDialog> dialogProvider,
             final MainFrame mainFrame,
             final IconManager iconManager,
             final DialogProvider<ChannelListDialog> channelListDialogProvider) {
         super("Channel");
         this.mainFrame = mainFrame;
         this.iconManager = iconManager;
         this.activeFrameManager = activeFrameManager;
         this.dialogProvider = dialogProvider;
         this.channelListDialogProvider = channelListDialogProvider;
         setMnemonic('c');
         addMenuListener(this);
         initChannelMenu();
         menuSelected(null);
     }
 
     /**
      * Initialises the channel menu.
      */
     private void initChannelMenu() {
         join = JMenuItemBuilder.create()
                .setText("Join Channle...")
                 .setMnemonic('j')
                 .addActionListener(e -> joinChannel())
                 .build();
         add(join);
 
         csd = JMenuItemBuilder.create()
                 .setMnemonic('c')
                 .setText("Channel Settings")
                 .addActionListener(l -> showChannelSettings()).build();
         add(csd);
 
         list = JMenuItemBuilder.create()
                 .setText("List channels...")
                 .setMnemonic('l')
                 .addActionListener(e -> channelListDialogProvider.displayOrRequestFocus())
                 .build();
         add(list);
     }
 
     private void joinChannel() {
         new StandardInputDialog(mainFrame,
                 Dialog.ModalityType.APPLICATION_MODAL, iconManager, "Join Channel",
                 "Enter the name of the channel to join.", this::doJoinChannel)
                 .displayOrRequestFocus();
     }
 
     private void showChannelSettings() {
         activeFrameManager.getActiveFrame().ifPresent(f -> {
             // TODO: Can't assume containers will be group chats.
             if (f.getContainer() instanceof GroupChat) {
                 dialogProvider.displayOrRequestFocus((GroupChat) f.getContainer());
             }
         });
     }
 
     private void doJoinChannel(final String text) {
         activeFrameManager.getActiveFrame()
                 .map(TextFrame::getContainer)
                 .flatMap(WindowModel::getConnection)
                 .map(Connection::getGroupChatManager)
                 .ifPresent(c -> c.join(new ChannelJoinRequest(text)));
     }
 
     @Override
     public final void menuSelected(final MenuEvent e) {
         final Optional<ServerState> activeConnectionState = activeFrameManager.getActiveFrame()
                 .map(TextFrame::getContainer)
                 .flatMap(WindowModel::getConnection)
                 .map(Connection::getState);
         final boolean connected = activeConnectionState.equals(Optional.of(ServerState.CONNECTED));
 
         join.setEnabled(connected);
         csd.setEnabled(connected);
         list.setEnabled(connected);
     }
 
     @Override
     public final void menuDeselected(final MenuEvent e) {
         //Ignore
     }
 
     @Override
     public final void menuCanceled(final MenuEvent e) {
         //Ignore
     }
 
 }
