 package game;
 
 import game.communications.tcpcomms.TCPClient;
 import game.communications.tcpcomms.TCPServer;
 
 import javax.swing.*;
 import javax.swing.Timer;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.*;
 
 public class ClientsDisplayPanel extends JPanel
 {
     private DefaultTableModel _clients = new DefaultTableModel(new Object[]{"Name", "IP Address", "Port"}, 0)
     {
         public boolean isCellEditable(int row, int column)
         {
             return false;
         }
 
     };
     private JTable _table = new JTable();
     private Map<Client, Long> _currentClients = new HashMap<Client, Long>();
 
     public ClientsDisplayPanel(final Grid grid, final Game game, final MakeBoard makeBoard)
     {
         setBackground(Color.lightGray);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Other players"));
 
         _table.setModel(_clients);
         _table.setFillsViewportHeight(true);
         _table.setPreferredScrollableViewportSize(new Dimension(180, 150));
         setRowHeight(_table);
         setColumnWidths(_table);
         _table.addMouseListener(new MouseAdapter()
         {
             public void mouseClicked(MouseEvent e)
             {
                 if(e.getClickCount() == 2)
                 {
                     int row = _table.rowAtPoint(e.getPoint());
 
                     final Client client = getSelectedClient(row);
                     if(row >= 0)
                     {
                         String text = "Do you want to play a game with " + client;
                         final NotificationDialog notificationDialog = new NotificationDialog(game.getFrame(), text);
                         notificationDialog.addYesButtonListener(new ActionListener()
                         {
                             public void actionPerformed(ActionEvent e)
                             {
                                 new TCPClient(client, grid, makeBoard, game).start();
                                 notificationDialog.setVisible(false);
                             }
                         });
                         notificationDialog.setVisible(true);
                     }
 
                 }
             }
         });
 
         _table.setDefaultRenderer(Object.class, new ColorRenderer());
 
         JScrollPane scrollPane = new JScrollPane(_table);
         add(scrollPane);
 
         _clients.addTableModelListener(new TableModelListener()
         {
             public void tableChanged(TableModelEvent e)
             {
                 _table.setModel(_clients);
             }
         });
 
         clearOldClients();
     }
 
     private void setColumnWidths(JTable table)
     {
         TableColumn column = null;
         for(int i = 0; i < table.getColumnCount(); i++)
         {
             column = table.getColumnModel().getColumn(i);
             if(i == 2)
             {
                 column.setPreferredWidth(50);
             }
             else
             {
                 column.setPreferredWidth(100);
             }
         }
     }
 
     private void setRowHeight(JTable table)
     {
         int height = table.getHeight();
         table.setRowHeight(height + 20);
     }
 
     private Client getSelectedClient(int row)
     {
         Set s = _currentClients.keySet();
         Iterator itr = s.iterator();
 
         int i = 0;
 
         while(itr.hasNext())
         {
             Client cl = (Client) itr.next();
             if(i == row)
             {
                 return cl;
             }
             i++;
         }
 
         return null;
     }
 
     public void clearOldClients()
     {
         Timer timer = new Timer(1, new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 for(Client c : new ArrayList<Client>(_currentClients.keySet()))
                 {
                     checkIfTimeOutExceeded(c, _currentClients.get(c));
                 }
             }
         });
         timer.setDelay(1000);
         timer.setRepeats(true);
         timer.start();
     }
 
     private void checkIfTimeOutExceeded(final Client c, final Long time)
     {
         long timeOut = time + 10000;
 
         if(timeOut < System.currentTimeMillis())
         {
             _currentClients.remove(c);
             displayClients();
         }
     }
 
     public void addClient(Client cl)
     {
         try
         {
             Client me = new Client(String.valueOf(TCPServer.getInstance().getPort()), InetAddress.getLocalHost(), System.getProperty("user.name"));
 
             if(!cl.equals(me))
             {
                 _currentClients.put(cl, System.currentTimeMillis());
             }
             displayClients();
         }
         catch(UnknownHostException e)
         {
             e.printStackTrace();
         }
     }
 
     private void displayClients()
     {
         int row = _table.getSelectedRow();
 
         _clients.setRowCount(0);
         for(Client cl : _currentClients.keySet())
         {
             _clients.addRow(new Object[]{cl.getName(), cl.getIPAddress(), cl.getPort()});
         }
 
         if(row >= 0)
         {
             _table.setRowSelectionInterval(row, row);
         }
     }
 
 
     private class ColorRenderer extends DefaultTableCellRenderer//JLabel implements TableCellRenderer
     {
         private final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
 
         public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column)
         {
             Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, column);
             Color foreground;
             Color background;
 
             if(isSelected)
             {
                 foreground = Color.darkGray;
                 background = Color.orange;
             }
             else
             {
                 if(row % 2 == 0)
                 {
                     foreground = Color.darkGray;
                     background = Color.white;
                 }
                 else
                 {
                     foreground = Color.darkGray;
                     background = Color.yellow;
                 }
             }
             renderer.setForeground(foreground);
             renderer.setBackground(background);
             return renderer;
         }
     }
 
 }
