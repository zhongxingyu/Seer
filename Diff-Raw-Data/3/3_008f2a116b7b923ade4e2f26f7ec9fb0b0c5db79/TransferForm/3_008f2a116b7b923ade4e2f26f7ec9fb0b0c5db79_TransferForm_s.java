 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.SystemColor;
 import java.awt.event.WindowEvent;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 public class TransferForm extends JFrame
 {
 	private Transfer transfer;
 
 	private JLabel statusLabel, detailLabel1, detailLabel2;
 
 	private JProgressBar progressBar;
 
 	private long lastUpdateTime = 0;
 
 	public TransferForm( final Transfer transfer )
 	{
 		this.transfer = transfer;
 		setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );
 
 		statusLabel = new JLabel();
 		statusLabel.setFont( statusLabel.getFont().deriveFont( Font.BOLD, 14 ) );
 		statusLabel.setAlignmentX( CENTER_ALIGNMENT );
 		detailLabel1 = new JLabel( " " );
 		detailLabel2 = new JLabel( " " );
 
 		detailLabel1.setAlignmentX( CENTER_ALIGNMENT );
 		detailLabel2.setAlignmentX( CENTER_ALIGNMENT );
 		detailLabel2.setForeground( SystemColor.controlDkShadow );
 
 		add( Box.createVerticalStrut( 10 ) );
 		add( statusLabel );
 		add( Box.createVerticalStrut( 5 ) );
 		add( detailLabel1 );
 		add( Box.createVerticalStrut( 3 ) );
 		add( detailLabel2 );
 		add( Box.createVerticalStrut( 2 ) );
 		add( Box.createVerticalGlue() );
 
 		JPanel progressPanel = new JPanel();
 		{
 			progressBar = new JProgressBar();
 			progressPanel.add( progressBar );
 			progressBar.setPreferredSize( new Dimension( 300, 25 ) );
 			progressPanel.setPreferredSize( new Dimension( 250, 40 ) );
 		}
 		add( progressPanel );
 		add( Box.createVerticalStrut( 3 ) );
 		add( Box.createVerticalGlue() );
 
 		updateComponents();
 		setMinimumSize( new Dimension( 400, 20 ) );
 		pack();
 		setResizable( false );
 
 		addWindowListener( new java.awt.event.WindowAdapter()
 		{
 			@SuppressWarnings( "deprecation" )
 			@Override
 			public void windowClosing( WindowEvent winEvt )
 			{
 				if ( !( transfer.getStage() == Transfer.Stage.TRANSFERRING || transfer.getStage() == Transfer.Stage.VERIFYING ) || JOptionPane.showConfirmDialog( null, "Are you sure you want to end this transfer?", "Cancel transfer", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION )
 				{
					transfer.transferFailed( "Cancelled" );
 					transfer.stop();
 					dispose();
 				}
 			}
 		} );
 	}
 
 	public void updateComponents()
 	{
 		if ( ( transfer.getStage() != Transfer.Stage.TRANSFERRING ) || ( System.currentTimeMillis() - lastUpdateTime > 100 ) )
 		{
 			setTitle( transfer.getName() );
 			progressBar.setValue( transfer.getProgress() );
 			statusLabel.setText( transfer.toString() );
 			detailLabel1.setText( transfer.getDetails() );
 			detailLabel2.setText( transfer.getTimeLeft() );
 			lastUpdateTime = System.currentTimeMillis();
 		}
 	}
 
 }
