 
 /***************************************************************************
  *   Copyright 2006-2007 by Christian Ihle                                 *
  *   kontakt@usikkert.net                                                  *
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program; if not, write to the                         *
  *   Free Software Foundation, Inc.,                                       *
  *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
  ***************************************************************************/
 
 package net.usikkert.kouchat.ui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.BorderFactory;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultEditorKit;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import net.usikkert.kouchat.misc.ChatWindow;
 import net.usikkert.kouchat.misc.CommandHistory;
 
 public class MainPanel extends JPanel implements ActionListener, CaretListener, ChatWindow, KeyListener, MouseListener
 {
 	private static final long serialVersionUID = 1L;
 	private static Logger log = Logger.getLogger( MainPanel.class.getName() );
 
 	private JScrollPane chatSP;
 	private JTextPane chatTP;
 	private MutableAttributeSet chatAttr;
 	private StyledDocument chatDoc;
 	private JTextField msgTF;
 	private Mediator mediator;
 	private CommandHistory cmdHistory;
 	private JPopupMenu chatMenu, msgMenu;
 	private JMenuItem chatCopyMI, msgCopyMI, pasteMI, cutMI;
 
 	public MainPanel( SidePanel sideP )
 	{
 		setLayout( new BorderLayout( 2, 2 ) );
 
 		chatTP = new JTextPane();
 		chatTP.setEditable( false );
 		chatTP.setBorder( BorderFactory.createEmptyBorder( 4, 6, 4, 6 ) );
 		chatTP.addMouseListener( this );
 		chatSP = new JScrollPane( chatTP );
 		chatAttr = new SimpleAttributeSet();
 		chatDoc = chatTP.getStyledDocument();
 
 		msgTF = new JTextField();
 		msgTF.addActionListener( this );
 		msgTF.addCaretListener( this );
 		msgTF.addKeyListener( this );
 		msgTF.addMouseListener( this );
 
 		add( chatSP, BorderLayout.CENTER );
 		add( sideP, BorderLayout.EAST );
 		add( msgTF, BorderLayout.SOUTH );
 		
 		chatCopyMI = new JMenuItem( new DefaultEditorKit.CopyAction() );
 		chatCopyMI.setText( "Copy" );
 		chatCopyMI.setMnemonic( 'C' );
 		chatCopyMI.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, KeyEvent.CTRL_MASK ) );
 		
 		msgCopyMI = new JMenuItem( new DefaultEditorKit.CopyAction() );
 		msgCopyMI.setText( "Copy" );
 		msgCopyMI.setMnemonic( 'C' );
 		msgCopyMI.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, KeyEvent.CTRL_MASK ) );
 		
 		cutMI = new JMenuItem( new DefaultEditorKit.CutAction() );
 		cutMI.setText( "Cut" );
 		cutMI.setMnemonic( 'U' );
 		cutMI.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, KeyEvent.CTRL_MASK ) );
 		
 		pasteMI = new JMenuItem( new DefaultEditorKit.PasteAction() );
 		pasteMI.setText( "Paste" );
 		pasteMI.setMnemonic( 'P' );
 		pasteMI.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, KeyEvent.CTRL_MASK ) );
 		
 		chatMenu = new JPopupMenu();
 		chatMenu.add( chatCopyMI );
 		
 		msgMenu = new JPopupMenu();
 		msgMenu.add( cutMI );
 		msgMenu.add( msgCopyMI );
 		msgMenu.add( pasteMI );
 
 		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 
 		cmdHistory = new CommandHistory();
 	}
 
 	public void setMediator( Mediator mediator )
 	{
 		this.mediator = mediator;
 	}
 
 	public void appendToChat( String text, int color )
 	{
 		try
 		{
 			StyleConstants.setForeground( chatAttr, new Color( color ) );
 			chatDoc.insertString( chatDoc.getLength(), text + "\n", chatAttr );
 			chatTP.setCaretPosition( chatDoc.getLength() );
 		}
 
 		catch ( BadLocationException e )
 		{
 			log.log( Level.SEVERE, e.getMessage(), e );
 		}
 	}
 
 	public void clearChat()
 	{
 		chatTP.setText( "" );
 	}
 
 	public JTextField getMsgTF()
 	{
 		return msgTF;
 	}
 	
 	public boolean isMenuVisible()
 	{
 		return chatMenu.isVisible() || msgMenu.isVisible();
 	}
 
 	public void caretUpdate( CaretEvent e )
 	{
 		SwingUtilities.invokeLater( new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				mediator.updateWriting();
 			}
 		} );
 	}
 
 	@Override
 	public void actionPerformed( ActionEvent e )
 	{
 		if ( e.getSource() == msgTF )
 		{
 			SwingUtilities.invokeLater( new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					cmdHistory.add( msgTF.getText() );
 					mediator.write();
 				}
 			} );
 		}
 	}
 
 	@Override
 	public void keyPressed( KeyEvent arg0 ) {}
 
 	@Override
 	public void keyTyped( KeyEvent arg0 ) {}
 
 	@Override
 	public void keyReleased( final KeyEvent ke )
 	{
 		SwingUtilities.invokeLater( new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if ( ke.getKeyCode() == KeyEvent.VK_UP )
 					msgTF.setText( cmdHistory.goUp() );
 				else if ( ke.getKeyCode() == KeyEvent.VK_DOWN )
 					msgTF.setText( cmdHistory.goDown() );
 			}
 		} );
 	}
 
 	@Override
 	public void mouseClicked( MouseEvent e ) {}
 
 	@Override
 	public void mouseEntered( MouseEvent e ) {}
 
 	@Override
 	public void mouseExited( MouseEvent e ) {}
 
 	@Override
 	public void mousePressed( MouseEvent e ) {}
 
 	@Override
 	public void mouseReleased( MouseEvent e )
 	{
 		if ( e.getSource() == chatTP )
 		{
 			if ( chatMenu.isPopupTrigger( e ) && chatTP.getSelectedText() != null )
 			{
 				chatMenu.show( chatTP, e.getX(), e.getY() );
 			}
 		}
 		
 		else if ( e.getSource() == msgTF )
 		{
 			if ( msgMenu.isPopupTrigger( e ) )
 			{
 				msgTF.requestFocus();
 				
 				if ( msgTF.getSelectedText() == null )
 				{
 					msgCopyMI.setEnabled( false );
 					cutMI.setEnabled( false );
 				}
 				
 				else
 				{
 					msgCopyMI.setEnabled( true );
 					cutMI.setEnabled( true );
 				}
 				
 				if ( Toolkit.getDefaultToolkit().getSystemClipboard().getContents( null ) == null )
 					pasteMI.setEnabled( false );
 				else
 					pasteMI.setEnabled( true );
 				
 				msgMenu.show( msgTF, e.getX(), e.getY() );
 			}
 		}
 	}
 }
