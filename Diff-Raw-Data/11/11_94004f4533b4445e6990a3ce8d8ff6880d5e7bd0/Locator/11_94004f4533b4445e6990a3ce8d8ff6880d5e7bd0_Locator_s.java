 package uk.co.brotherlogic.mdb.finder;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.format.GetFormats;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 public class Locator extends JFrame
 {
    Map<String, String> catMap = new TreeMap<String, String>();
    Map<String, String> befores = new TreeMap<String, String>();
    Map<String, String> afters = new TreeMap<String, String>();
 
    JTextArea mainArea;
 
    JTextField entryField;
 
    JFrame parent = null;
 
    public Locator()
    {
 
    }
 
    private void buildList()
    {
       // Connect to the database
       List<Record> records = new LinkedList<Record>();
 
       try
       {
          // Select the baseFormat
          Set<String> baseFormats = new TreeSet<String>(GetFormats.create().getBaseFormats());
          String[] arr = baseFormats.toArray(new String[0]);
          int chosen = JOptionPane.showOptionDialog(null, "Choose Base Format", "Base Format",
                JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, arr, "");
 
          records.addAll(GetRecords.create().getRecords(GetRecords.SHELVED, arr[chosen]));
          Collections.sort(records, new Comparator<Record>()
          {
             @Override
             public int compare(Record o1, Record o2)
             {
                return o1.getShelfPos().compareTo(o2.getShelfPos());
             }
          });
 
          // Add the unshelved
          Collection<Record> recs = GetRecords.create()
                .getRecords(GetRecords.UNSHELVED, arr[chosen]);
          for (Record rec : recs)
          {
             rec.setCategory(new Category("Unshelved", 10));
             System.err.println(rec.getAuthor() + " - " + rec.getTitle());
          }
          records.addAll(recs);
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
 
      for (int i = 1; i < records.size() - 1; i++)
       {
          Record rec = records.get(i);
          catMap.put(rec.getDisplayTitle(), rec.getCategory().getCatName());
         befores.put(rec.getDisplayTitle(), records.get(i - 1).getDisplayTitle());
         afters.put(rec.getDisplayTitle(), records.get(i + 1).getDisplayTitle());
       }
    }
 
    private void filter()
    {
       // Init the gui
       mainArea = new JTextArea();
       mainArea.setEditable(false);
       entryField = new JTextField();
 
       GridBagLayout gbl = new GridBagLayout();
       this.setLayout(gbl);
 
       gbl.setConstraints(entryField, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
             GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.add(entryField);
 
       gbl.setConstraints(mainArea, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
             GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.add(mainArea);
       this.setVisible(true);
       this.setExtendedState(JFrame.MAXIMIZED_BOTH);
 
       this.addWindowListener(new WindowAdapter()
       {
 
          @Override
          public void windowClosing(WindowEvent e)
          {
             parent.setVisible(true);
          }
 
       });
 
       entryField.addKeyListener(new KeyAdapter()
       {
          @Override
          public void keyTyped(KeyEvent e)
          {
             show(entryField.getText());
          }
       });
    }
 
    public void run(JFrame parent)
    {
       buildList();
       filter();
       this.parent = parent;
    }
 
    private void show(String part)
    {
       StringBuffer toShow = new StringBuffer();
       int lcount = 0;
       for (Entry<String, String> entry : catMap.entrySet())
          if (entry.getKey().toLowerCase().contains(part.toLowerCase()))
          {
             toShow.append((entry.getValue() + "\n" + befores.get(entry.getKey()) + "\n"
                   + entry.getKey() + "\n" + afters.get(entry.getKey()))
                   + "\n------------------\n");
             lcount++;
          }
 
       mainArea.setText(toShow.toString());
    }
 
 }
