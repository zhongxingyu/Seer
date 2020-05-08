 package gov.nih.nci.ncicb.cadsr.loader.ui;
 import gov.nih.nci.ncicb.cadsr.domain.AdminComponent;
 import gov.nih.nci.ncicb.cadsr.domain.Concept;
 import gov.nih.nci.ncicb.cadsr.domain.DataElementConcept;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.domain.ObjectClass;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ext.EvsModule;
 import gov.nih.nci.ncicb.cadsr.loader.ext.EvsResult;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.SearchEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.SearchListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 
 
 
 public class ValidateConceptsDialog extends JDialog 
   implements ListSelectionListener
 {
   private EvsModule module = new EvsModule();
   private Map<Concept, Concept> errorList = new HashMap<Concept, Concept>();
   private Map<Concept, Concept> errorNameList = new HashMap<Concept, Concept>();
 
   private List<AcListElementWrapper> wrapperList = 
     new ArrayList<AcListElementWrapper>();
   
   private JSplitPane jSplitPane1 = new JSplitPane();
   private JSplitPane jSplitPaneEvs = new JSplitPane();
   
   private JEditorPane elementPane = new JEditorPane("text/html", "");
   private JEditorPane evsByCodePane = new JEditorPane("text/html", "");
   private JEditorPane evsByNamePane = new JEditorPane("text/html", "");
     
   private DefaultListModel listModel = new DefaultListModel();
   private JList list;
   private JScrollPane listScrollPane;
     
   private List<Concept> highlightDifferentNameByCode = new ArrayList<Concept>();
   private List<Concept> highlightDifferentDefByCode = new ArrayList<Concept> ();
   private List<Concept> highlightDifferentCodeByName = new ArrayList<Concept> ();
   private List<Concept> highlightDifferentDefByName = new ArrayList<Concept> ();
   
   private AbstractTableModel tableModel = null;
   private JTable resultTable = null;
   
   private ProgressPanel progressPanel = new ProgressPanel(100);
   
   private String[] columnNames = {
     "Element Concept", "EVS Concept"
   };
 
   private int colWidth[] = {30, 30};
 
   private static int PAGE_SIZE = 5;
 
   private int pageIndex = 0;
   
   private java.util.List<AdminComponent> resultSet = new ArrayList<AdminComponent>();
   
   private AcListElementWrapper value;
   
   private JLabel order;
   
   private List<SearchListener> searchListeners = new ArrayList();
   
   public ValidateConceptsDialog(JFrame owner)
   {
     super(owner, "Validate Concepts");
     this.getContentPane().setLayout(new BorderLayout());
 
 
     SwingWorker worker = new SwingWorker() {
       public Object construct() {    
         List<Concept> concepts = ElementsLists.getInstance().
             getElements(DomainObjectFactory.newConcept());
         
         ProgressEvent event = new ProgressEvent();
         event.setGoal(concepts.size() + 1);
         event.setMessage("Validating Concepts");
         progressPanel.newProgressEvent(event);
         
         int pStatus = 0;
         for(Concept con : concepts) 
         {
           event = new ProgressEvent();
           event.setMessage("Validating " + con.getLongName());
           event.setStatus(pStatus++);
           progressPanel.newProgressEvent(event);
           
           //EvsResult result = null;
           
           //if(con.getPreferredName() != null) {
             EvsResult result = module.findByConceptCode(con.getPreferredName(), false);
           //}
           if(result != null) 
          {  
              if(con.getLongName() == null || !con.getLongName().equals(result.getConcept().getLongName())) {
                 highlightDifferentNameByCode.add(result.getConcept());
                errorList.put(con, result.getConcept());
              }
              if(con.getPreferredDefinition() == null || !con.getPreferredDefinition().trim().equals(result.getConcept().getPreferredDefinition().trim())) {
                 highlightDifferentDefByCode.add(result.getConcept());
                errorList.put(con, result.getConcept());
              }
           }
           
           Collection<EvsResult> nameResult = module.findByPreferredName(con.getLongName(), false);
           if(nameResult != null && nameResult.size() == 1) 
           {
             for(EvsResult name : nameResult) { 
            if(con.getPreferredName() == null || !con.getPreferredName().equals(name.getConcept().getPreferredName())) { 
              highlightDifferentCodeByName.add(name.getConcept());
               errorNameList.put(con, name.getConcept());
            }
            if(con.getPreferredDefinition() == null || !con.getPreferredDefinition().trim().equals(name.getConcept().getPreferredDefinition().trim())) { 
               highlightDifferentDefByName.add(name.getConcept());      
              errorNameList.put(con, name.getConcept());
             }
           }
         }
        }
         List<ObjectClass> ocs = ElementsLists.getInstance().
             getElements(DomainObjectFactory.newObjectClass());
         
         List<DataElementConcept> decs = ElementsLists.getInstance().
             getElements(DomainObjectFactory.newDataElementConcept());
         
         for(ObjectClass oc : ocs)
           for(Concept concept : errorList.keySet()) {
             String temp = oc.getPreferredName();
             String split[] = temp.split(":");
             for(int i = 0; i < split.length; i++)
               if(split[i].equals(concept.getPreferredName()))
                 wrapperList.add(new AcListElementWrapper(oc, concept));
           }
         
         for(DataElementConcept dec : decs)
           for(Concept concept : errorList.keySet()) {
             String temp = dec.getProperty().getPreferredName();
             String split[] = temp.split(":");
             for(int i = 0; i < split.length; i++)
               if(split[i].equals(concept.getPreferredName()))
                 wrapperList.add(new AcListElementWrapper(dec, concept));
           }
         
         for(ObjectClass oc : ocs)
           for(Concept concept : errorNameList.keySet()) {
             String temp = oc.getLongName();
               if(temp.equals(concept.getLongName()))
                 wrapperList.add(new AcListElementWrapper(oc, concept));
           }
         
         for(DataElementConcept dec : decs)
           for(Concept concept : errorNameList.keySet()) {
             String temp = dec.getProperty().getLongName();
               if(temp.equals(concept.getLongName()))
                 wrapperList.add(new AcListElementWrapper(dec, concept));
           }
         
         
         event = new ProgressEvent();
         event.setMessage("Done ");
         event.setStatus(100);
         event.setGoal(100);
         progressPanel.newProgressEvent(event);
          
         for(AcListElementWrapper val : wrapperList)
           listModel.addElement(val);
           
           return null;
       }
     };
     worker.start();
         
         final int LIST_SIZE = 390;
         
         list = new JList(listModel);
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listScrollPane = new JScrollPane(list);        
         
         list.addListSelectionListener(this);
         list.setCellRenderer(new MyCellRenderer());
         
         JPanel mainPanel = new JPanel();
         order = new JLabel("");
         order.setBounds(new Rectangle(110, 0, 220, 20));
         mainPanel.setSize(new Dimension(400, 440));
         mainPanel.setLayout(null);
         listScrollPane.setBounds(new Rectangle(10, 30, 140, LIST_SIZE));
         
         jSplitPane1.setBounds(new Rectangle(160, 30, 220, LIST_SIZE));
 
         jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
 
         jSplitPane1.setDividerLocation((int)(LIST_SIZE * 2 / 3));
         jSplitPaneEvs.setOrientation(JSplitPane.VERTICAL_SPLIT);
         
         
         evsByCodePane.setBorder(javax.swing.BorderFactory.createTitledBorder("EVS Concept By Code"));
         elementPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Element Concept"));
         evsByNamePane.setBorder(javax.swing.BorderFactory.createTitledBorder("EVS Concept By Name"));
         
         elementPane.setEditable(false);
         evsByCodePane.setEditable(false);
         evsByNamePane.setEditable(false);
         
         JScrollPane elementScrollPane = new JScrollPane(elementPane);
         JScrollPane evsByCodeScrollPane = new JScrollPane(evsByCodePane);
         JScrollPane evsByNameScrollPane = new JScrollPane(evsByNamePane);
         
         
         jSplitPaneEvs.setDividerLocation(LIST_SIZE / 3);
         jSplitPaneEvs.add(evsByCodeScrollPane, JSplitPane.BOTTOM);
         jSplitPaneEvs.add(evsByNameScrollPane, JSplitPane.TOP);
         
         jSplitPane1.add(elementScrollPane, JSplitPane.BOTTOM);
         jSplitPane1.add(jSplitPaneEvs, JSplitPane.TOP);
         
         mainPanel.add(jSplitPane1, null);
         mainPanel.add(listScrollPane, null);
         mainPanel.add(order);
         
         
         this.getContentPane().setLayout(new BorderLayout());
         this.getContentPane().add(mainPanel, BorderLayout.CENTER);
             
         this.getContentPane().add(new JLabel("Select an element from the list to view the concepts' information"));
         this.setSize(400, 510);
         this.getContentPane().add(progressPanel, BorderLayout.SOUTH);
         this.setResizable(false);
         UIUtil.putToCenter(this);
         
 
   }
   
   public void valueChanged(ListSelectionEvent e) 
   {
     if (e.getValueIsAdjusting() == false) {
       if (list.getSelectedIndex() != -1) {
         value = (AcListElementWrapper)list.getSelectedValue();
         order.setText(value.getOrder());
         elementPane.setText(getConceptHtml(value.getConcept()));
         evsByCodePane.setText(getHighlightConceptHtmlByCode(errorList.get(value.getConcept())));
         evsByNamePane.setText(getHighlightConceptHtmlByName(errorNameList.get(value.getConcept())));
                 
         int ind = value.getAc().getLongName().lastIndexOf(".");
         String className = value.getAc().getLongName().substring(ind + 1);
         String split[] = className.split(":");
         if(split.length > 1) {
           className = split[0];
           SearchEvent searchEvent = new SearchEvent(className, false,false,true);
           fireSearchEvent(searchEvent);
           className = split[1];
           SearchEvent searchEvent2 = new SearchEvent(className, false,false,false);
           fireSearchEvent(searchEvent2);
         }  
         else {
           SearchEvent searchEvent = new SearchEvent(className, false,false,true);
           fireSearchEvent(searchEvent);
         }
       }
     }
   }
   
   private String getHighlightConceptHtmlByCode(Concept con) 
   {
     StringBuilder sb = new StringBuilder();
     if(con != null) {
       sb.append("<b>Code: </b>" + con.getPreferredName() + "<br>");
       if(highlightDifferentNameByCode.contains(con))
         sb.append("<div bgcolor='yellow'>" + "<b>Preferred Name: </b>" + con.getLongName() + "</div>" + "<br>");
       else
         sb.append("<b>Preferred Name: </b>" + con.getLongName() + "<br>");
       if(highlightDifferentDefByCode.contains(con))
         sb.append("<div bgcolor='yellow'>" + "<b>Definition: </b>" + con.getPreferredDefinition() + "</div>" + "<br>");
       else
         sb.append("<b>Definition: </b>" + con.getPreferredDefinition() + "<br>");
       
       sb.append("</body></html>");
     }
     return sb.toString();
   }
   
   private String getHighlightConceptHtmlByName(Concept con) 
   {
     StringBuilder sb = new StringBuilder();
     if(con != null) {
       if(highlightDifferentCodeByName.contains(con))
         sb.append("<div bgcolor='yellow'>" + "<b>Code: </b>" + con.getPreferredName() + "</div>" + "<br>");
       else
         sb.append("<b>Code: </b>" + con.getPreferredName() + "<br>");
       sb.append("<b>Preferred Name: </b>" + con.getLongName() + "<br>");
       if(highlightDifferentDefByName.contains(con))
         sb.append("<div bgcolor='yellow'>" + "<b>Definition: </b>" + con.getPreferredDefinition() + "</div>" + "<br>");
       else
         sb.append("<b>Definition: </b>" + con.getPreferredDefinition() + "<br>");
       
       sb.append("</body></html>");
     }
     return sb.toString();
   }
   
   
   private String getConceptHtml(Concept con) 
   {
     StringBuilder sb = new StringBuilder();
     if(con != null) {
       sb.append("<html><body>");
       sb.append("<b>Code: </b>" + con.getPreferredName() + "<br>");
       sb.append("<b>Preferred Name: </b>" + con.getLongName() + "<br>");
       sb.append("<b>Definition: </b>" + con.getPreferredDefinition() + "<br>");
       sb.append("</body></html>");
     }
 
     return sb.toString();
   }
   
   public void addSearchListener(SearchListener listener) 
   {
     searchListeners.add(listener);
   }
   
   public void fireSearchEvent(SearchEvent event) 
   {
     for(SearchListener l : searchListeners)
       l.search(event);
   }
   
   class AcListElementWrapper<T extends AdminComponent>
 {
   private T ac;
   private Concept con;
   
   AcListElementWrapper(T ac, Concept con) 
   {
     this.ac = ac;
     this.con = con;
   }
   T getAc() 
   {
     return ac;
   }
   
   Concept getConcept() 
   {
     return con;
   }
   
   String getOrder() 
   {
     String order = "";
     int index = -1;
     if(ac instanceof ObjectClass) {
       String prefName = ac.getPreferredName();
       String split[] = prefName.split(":");
   
       for(int i = 0; i < split.length; i++)
         if(split[i].equals(con.getPreferredName()))
           index = i;
     
       if(split.length -1 == index)
         order = "Class Primary Concept";
       else
         order = "Class Qualifier Concept #" + (split.length - 1 - index);
     }
     
     if(ac instanceof DataElementConcept) {
       String prefName = ((DataElementConcept)ac).getProperty().getPreferredName();;
       String split[] = prefName.split(":");
 
       for(int i = 0; i < split.length; i++)
         if(split[i].equals(con.getPreferredName()))
           index = i;
     
       if(split.length -1 == index)
         order = "Attribute Primary Concept";
       else
         order = "Attribute Qualifier Concept #" + (split.length - 1 - index);
     }
     return order;
   }
   
 }
 class MyCellRenderer extends JLabel implements ListCellRenderer {
   public MyCellRenderer() {
          setOpaque(true);
      }
   public Component getListCellRendererComponent(
          JList list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus)
      {
   
         AcListElementWrapper acW = (AcListElementWrapper)value;
         
         int ind = acW.getAc().getLongName().lastIndexOf(".");
         String className = acW.getAc().getLongName().substring(ind + 1);
         setText(className);
         if (isSelected) {
              setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
          else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
         setEnabled(list.isEnabled());
         return this;
      }
 
 }
 
 }
 
 
