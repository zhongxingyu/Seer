 package cytoscape.dialogs;
 
 import edu.umd.cs.piccolox.event.PNotificationCenter;
 import edu.umd.cs.piccolox.event.PNotification;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import java.awt.event.*;
 
 import java.util.*;
 import java.awt.*;
 
 import ViolinStrings.Strings;
 
 import cytoscape.data.*;
 import cytoscape.view.*;
 import cytoscape.*;
 import cytoscape.CyNetwork;
 import giny.model.*;
 import giny.view.*;
 import phoebe.*;
 
 public class GraphObjectSelection extends JPanel implements ActionListener {
 
   JTextField searchField;
   JCheckBox regexpSearch, clearSelection;
   JRadioButton hideFailed, grayFailed, selectPassed;
   JList selectedAttributes;
   JList allAttributes;
   CyNetwork cyNetwork;
   GraphObjAttributes nodeAttributes;
   GraphObjAttributes edgeAttributes;
   PGraphView graphView;
   CyNetworkView networkView;
 
   public GraphObjectSelection (   ) {
    
     initialize();
   }
 
   protected void initialize () {
 
     networkView = Cytoscape.getCurrentNetworkView();
     cyNetwork = networkView.getNetwork();
     nodeAttributes = Cytoscape.getNodeNetworkData();
     edgeAttributes = Cytoscape.getEdgeNetworkData();
 
     // Create the Node Selection Panel
     JPanel searchPanel = new JPanel();
     searchPanel.setBorder( new TitledBorder( "Describe Filter" ) );
     searchPanel.add( new JLabel( "Filter: " ) );
     searchField = new JTextField( 30 );
     searchField.addActionListener( this );
     searchPanel.add( searchField );
     regexpSearch = new JCheckBox( "Regexp?" );
     searchPanel.add( regexpSearch );
     searchPanel.add( new JButton (new AbstractAction( "Go!" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   // do the search
                   performSearch();
                 }
               } ); } } ) );
 
     // Create the All Attributes List Panel
     JPanel allAttributesPanel = new JPanel();
     allAttributesPanel.setBorder( new TitledBorder( "All Available String Attributes" ) );
 
 
     String[] node_attribute_names = nodeAttributes.getAttributeNames();
     ArrayList string_attributes = new ArrayList( node_attribute_names.length );
     ArrayList number_attributes = new ArrayList( node_attribute_names.length );
     ArrayList other_attributes = new ArrayList( node_attribute_names.length );
 
     for ( int i = 0; i < node_attribute_names.length; ++i ) {
      Class type = GraphObjAttributesImpl.deduceClass( node_attribute_names[i] );
 
       System.out.println( "Attr: "+node_attribute_names[i]+" Class: "+type.getName() );
 
 
       if ( type.getName().equals( String.class.getName() ) ) {
         string_attributes.add( node_attribute_names[i] );
       } else if (  type.getName().equals( Double.class.getName() ) ) {
         number_attributes.add( node_attribute_names[i] );
       } else {
         other_attributes.add( node_attribute_names[i] );
       }
     }
 
     DefaultListModel model_nodes = new DefaultListModel();
     Iterator string_atts = string_attributes.iterator();
     for (  int i = 0; string_atts.hasNext(); ++i  ) {
       model_nodes.add( i, ( String )string_atts.next() );
     }
     allAttributes = new JList( model_nodes );
     JScrollPane scrollPaneAll = new JScrollPane();
     scrollPaneAll.getViewport().setView( allAttributes );
     allAttributesPanel.add( scrollPaneAll );
 
     // Create the Selection Panel
     JPanel selectedAttributesPanel = new JPanel();
     selectedAttributesPanel.setBorder( new TitledBorder( "Selected Attributes" ) );
     selectedAttributes = new JList( new DefaultListModel() );
     JScrollPane scrollPaneSel = new JScrollPane();
     scrollPaneSel.getViewport().setView( selectedAttributes );
     allAttributesPanel.add( scrollPaneSel );
     selectedAttributesPanel.add( scrollPaneSel );
 
     // Create the Center Panel
     JPanel centerPanel = new JPanel();
     centerPanel.setLayout( new GridLayout( 0, 1 ) );
     
     JPanel controlPanel = new JPanel();
     controlPanel.setBorder( new TitledBorder( "Control" ) );
     controlPanel.add(  new JButton (new AbstractAction( "+" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   // Add the Attributes selected in the all attributes list to the selectionlist
                   
                   // get the selected attributes
                   Object[] attributes = allAttributes.getSelectedValues();
                 
                   // add them to the selection list
                   Set current_selection = new TreeSet( java.util.Arrays.asList( ( ( DefaultListModel )selectedAttributes.getModel() ).toArray() ) );
                   current_selection.addAll( java.util.Arrays.asList(  attributes ) );
                   DefaultListModel new_model = new DefaultListModel();
                   Iterator sel = current_selection.iterator();
                   while ( sel.hasNext() ){
                     new_model.addElement( sel.next() );
                   }
                   selectedAttributes.setModel( new_model );
                 }
               } ); } } ) );
     controlPanel.add(  new JButton (new AbstractAction( "-" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   // Remove all Selected Attributes in the Selection List
                   Object[] attributes = selectedAttributes.getSelectedValues();
                   DefaultListModel dlm = ( DefaultListModel )selectedAttributes.getModel();
                   for ( int i = 0; i < attributes.length; ++i ) {
                     dlm.removeElement( attributes[i] );
                   }
 
                 }
               } ); } } ) );
     controlPanel.add(  new JButton (new AbstractAction( "Update" ) {
           public void actionPerformed ( ActionEvent e ) {
             // Do this in the GUI Event Dispatch thread...
             SwingUtilities.invokeLater( new Runnable() {
                 public void run() {
                   // update the integration view
                   //integration.updateView();                
                   
                 }
               } ); } } ) );
     
     clearSelection = new JCheckBox( "Clear", false );
     controlPanel.add( clearSelection );
     centerPanel.add( controlPanel );
 
     JPanel actionPanel = new JPanel();
     ButtonGroup actionGroup = new ButtonGroup();
     actionPanel.setBorder( new TitledBorder( "Action to Take" ) );
     
     hideFailed = new JRadioButton( "Hide Failed", true );
     actionGroup.add( hideFailed );
     actionPanel.add( hideFailed );
 
     
     grayFailed = new JRadioButton( "Gray Failed", false );
     actionGroup.add( grayFailed );
     actionPanel.add( grayFailed );
 
     selectPassed = new JRadioButton( "Select Passed", false );
     actionGroup.add( selectPassed );
     actionPanel.add( selectPassed );
 
     centerPanel.add( actionPanel );
 
     JSplitPane all_center_1 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, true,  centerPanel, allAttributesPanel  );
     JSplitPane sel = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, true, selectedAttributesPanel, all_center_1 );
     JSplitPane all = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true, searchPanel, sel );
     add( all );
 
   }
 
    public Object[] getSelectionList () {
     return ( ( DefaultListModel )selectedAttributes.getModel() ).toArray();
   }
 
    public void actionPerformed ( ActionEvent event ) {
      // when ENTER is pressed do a search
      this.graphView = (PGraphView)networkView.getView();
     performSearch();    
     
     // update the view
 
   } // actionPerformed
 
   public void performSearch () {
 
     ArrayList passes = new ArrayList();
     Object[] selected_attributes_o = getSelectionList();
     String[] selected_attributes = new String[selected_attributes_o.length];
     for ( int i = 0; i<selected_attributes_o.length; ++i ) {
       selected_attributes[i] = ( String )selected_attributes_o[i];
     }
 
 
     if ( regexpSearch.isSelected() ) {
      
       System.out.println( "not Implemented" );
 
     } else {
 
       String[] pattern = searchField.getText().split("\\s");
       String[] nodes_with_attribute;
      
         for ( int i = 0; i < selected_attributes.length; ++i ) {
           
           nodes_with_attribute = nodeAttributes.getObjectNames( selected_attributes[i] );
           for ( int j =0; j < nodes_with_attribute.length; ++j ) {
             // get the String Value for the node for the given attribute
             String value = nodeAttributes.getStringValue( selected_attributes[i], 
                                                           nodes_with_attribute[j] );
         
             for ( int p = 0; p < pattern.length; ++p ) 
               if ( Strings.isLike( value, pattern[p], 0, true ) ) {
                 // this means that:
                 // the value, which is associated with a given selected attribute
                 //     and a node name is like one of the strings in the search
                 //     box, which includes wildcards.
                 //passes.add( graphView.getNodeView( nodes_with_attribute[j] ) );
                 //System.out.println( nodes_with_attribute[j]+" Matches Pattern: "+pattern[p]+" on Attribute: "+selected_attributes[i]+" and the object is a: "+ ( nodeAttributes.getGraphObject( nodes_with_attribute[j]) ).getClass().getName() );
                 //passes.add( graphView.getNodeView( ( Node )nodeAttributes.getGraphObject( nodes_with_attribute[j]) ) );
                 //System.out.println( "This got added to Passed: "+graphView.getNodeView( ( Node )nodeAttributes.getGraphObject( nodes_with_attribute[j]) ) );
                 passes.add( nodes_with_attribute[j] );
 
               }
           }
         }
     }
 
 //     Iterator views = networkView.getView().getNodeViewsList().iterator();
 //     while ( views.hasNext() ) {
 //       System.out.println( "On Crack: "+views.next() );
 //     }
 
 
     // Hide the Failed Nodes
     if ( hideFailed.isSelected() ) {
       
       // restore all EdgeViews prior to hiding
       graphView.showGraphObjects( graphView.getEdgeViewsList() );
       Iterator all_nodes = networkView.getView().getGraphPerspective().nodesList().iterator();
       while ( all_nodes.hasNext() ) {
         Node node =  ( Node )all_nodes.next();
         if ( passes.contains( node.getIdentifier() ) ) {
           graphView.showNodeView( graphView.getNodeView( node ), false );
         } else {
           graphView.hideNodeView(  graphView.getNodeView( node ) );
         }
       }
     } 
     // gray the Failed
     else if ( grayFailed.isSelected() ) {
       graphView.showGraphObjects( graphView.getEdgeViewsList() );
       graphView.showGraphObjects( graphView.getNodeViewsList() );
       Iterator all_nodes = graphView.getGraphPerspective().nodesList().iterator();
       while ( all_nodes.hasNext() ) {
         Node node =  ( Node )all_nodes.next();
         if ( passes.contains( node.getIdentifier() ) ) {
           graphView.getNodeView( node ).setTransparency( 1f );
         } else {
           graphView.getNodeView( node ).setTransparency( 0.5f );
         }
       }
     } 
     // select those who passed 
     else if ( selectPassed.isSelected() ) {
       graphView.showGraphObjects( graphView.getEdgeViewsList() );
       graphView.showGraphObjects( graphView.getNodeViewsList() );
       Iterator all_nodes = graphView.getGraphPerspective().nodesList().iterator();
       while ( all_nodes.hasNext() ) {
         Node node =  ( Node )all_nodes.next();
         if ( passes.contains( node.getIdentifier() ) ) {
           graphView.getNodeView( node ).setSelected( true );
         } else {
           graphView.getNodeView( node ).setSelected( false );
         }
       }
 
     }
     
   }
 
 }
