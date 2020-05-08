 package taberystwyth.view;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import taberystwyth.controller.AllocationFrameListener;
 import taberystwyth.prelim.DrawTypeRepository;
 
 import net.miginfocom.swing.MigLayout;
 
 public class AllocationFrame extends JFrame {
 	
 	AllocationFrameListener listener = new AllocationFrameListener();
 	
 	DrawTypeRepository repos = DrawTypeRepository.getInstance();
 	
 	JLabel teamDrawTypeLabel = new JLabel("Draw teams with: ");
 	JTextArea teamDrawTypeDescription = new JTextArea();
 	JComboBox teamDrawTypeBox = new JComboBox();
 	DefaultComboBoxModel teamDrawTypes = new DefaultComboBoxModel();
 	
 	JLabel locationDrawTypeLabel = new JLabel("Draw locations with: ");
 	JTextArea locationDrawTypeDescription = new JTextArea();
 	JComboBox locationDrawTypeBox = new JComboBox();
 	DefaultComboBoxModel locationDrawTypes = new DefaultComboBoxModel();
 
 	JLabel judgeDrawTypeLabel = new JLabel("Draw judges with: ");
 	JTextArea judgeDrawTypeDescription = new JTextArea();
 	JComboBox judgeDrawTypeBox = new JComboBox();
 	DefaultComboBoxModel judgeDrawTypes = new DefaultComboBoxModel();
 
 
 	JButton cancel = new JButton("Cancel");
 	JButton allocate = new JButton("Allocate");
 	
     
     private static final long serialVersionUID = 1L;
     private static AllocationFrame instance = new AllocationFrame();
     
     public static AllocationFrame getInstance() {
         return instance;
     }
     
     private AllocationFrame(){
         setLayout(new MigLayout("wrap 2, flowx, fillx", "[left]rel[right]"));
         setTitle("Allocate");
         
         /*
          * When the window is closed, make it invisible
          */
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent evt) {
                 AllocationFrame.getInstance().setVisible(false);
             }
         });
         
         
         /*
          * Set up description boxes
          */
         teamDrawTypeDescription.setWrapStyleWord(true);
         locationDrawTypeDescription.setWrapStyleWord(true);
         judgeDrawTypeDescription.setWrapStyleWord(true);
 
         
         
         /*
          * Set up comboboxes
          */      
         for(String name : repos.getTeamDrawTypeMap().keySet()){        	
         	teamDrawTypes.addElement(name);
         }
         
         for(String name : repos.getJudgeDrawTypeMap().keySet()){        	
         	judgeDrawTypes.addElement(name);
         }
 
         for(String name : repos.getLocationDrawTypeMap().keySet()){        	
         	locationDrawTypes.addElement(name);
         }
         
         teamDrawTypeBox.setModel(teamDrawTypes);
         judgeDrawTypeBox.setModel(judgeDrawTypes);
         locationDrawTypeBox.setModel(locationDrawTypes);
         
         /*
          * Set up descriptions
          */
         
         add(teamDrawTypeLabel);
         add(teamDrawTypeBox);
        add(teamDrawTypeDescription, "wrap");
         
         add(judgeDrawTypeLabel);
         add(judgeDrawTypeBox);
        add(judgeDrawTypeDescription, "wrap");
 
         add(locationDrawTypeLabel);
         add(locationDrawTypeBox);
        add(locationDrawTypeDescription, "wrap");
 
 
         
         
         add(cancel, "tag cancel");
         add(allocate, "tag apply");
         pack();
         setMinimumSize(getSize());
         
         /*
          * Set up listener
          */
         cancel.addActionListener(listener);
         allocate.addActionListener(listener);
         
         updateDescriptions();
     }
     
     public void updateDescriptions(){
     	teamDrawTypeDescription.setText(
     			repos.getTeamDrawTypeMap().get(teamDrawTypeBox.getSelectedItem()));
     	judgeDrawTypeDescription.setText(
     			repos.getJudgeDrawTypeMap().get(judgeDrawTypeBox.getSelectedItem()));
     	locationDrawTypeDescription.setText(
     			repos.getLocationDrawTypeMap().get(locationDrawTypeBox.getSelectedItem()));
     	
     }
     
 }
