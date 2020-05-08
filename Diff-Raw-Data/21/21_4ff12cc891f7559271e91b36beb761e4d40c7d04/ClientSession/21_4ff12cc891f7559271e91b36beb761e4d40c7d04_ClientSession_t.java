 package sessions;
 
 import actions.AcquireCreditsAction;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumn;
 import musics.Music;
 import formcomponents.ClientCatalogTableModel;
 import formcomponents.MyMusicTableModel;
 import dialogs.AcquireCreditsDialog;
 import dialogs.ClientFrame;
 import formcomponents.DialogWithCatalogInterface;
 import formcomponents.DialogWithCreditsInterface;
 import formcomponents.HeaderListener;
 import formcomponents.TableButton;
 import formcomponents.TableButtonListener;
 import users.Client;
 
 public class ClientSession extends UserSession {
     
     private List<Music> myMusic;
     private String myMusicFilterField = "";
     private String myMusicFilterKeywords = "";
     private String myMusicSortField = "";
     
     public ClientSession(Client client) {
         super(client);
     }
      
     public Client getClient() {
         return (Client) super.getUser();
     }
     
     public void buy(Music music) {
         try {
             if (JOptionPane.showConfirmDialog(component, "Você realmente deseja comprar a música "+music.getName()+"?", "Tchu Tcha Tcha Store", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                 this.getClient().buy(music);
                JOptionPane.showMessageDialog(component, "A compra foi realizada com sucesso!", "Tchu Tcha Tcha Store", JOptionPane.INFORMATION_MESSAGE);
                this.buildGreetings();
                 this.reloadMyMusic();
                 this.reloadCatalog();
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(component, e.getMessage(), "Tchu Tcha Tcha Store", JOptionPane.ERROR_MESSAGE);
         }
     }
     
     public void reloadMyMusic() {
         myMusic = this.getClient().getMusics();
         myMusic = Music.filterMusicList(myMusic, myMusicFilterField, myMusicFilterKeywords);
         myMusic = Music.sortMusicList(myMusic, myMusicSortField);
         this.buildMyMusic();
     }
 
     public void filterMyMusic(String field, String keywords) {
         myMusicFilterField = map.get(field);
         myMusicFilterKeywords = keywords;
         this.reloadMyMusic();
     }
     
     public void sortMyMusics(String field) {
         myMusicSortField = map.get(field);
         myMusic = Music.sortMusicList(myMusic, myMusicSortField);
         this.buildMyMusic();
     }
     
     public void buildMyMusic() {
         final JTable publicationsTable = ((ClientFrame) component).getMyMusicTable();     
         publicationsTable.setModel(new MyMusicTableModel(myMusic));
         JTableHeader header = publicationsTable.getTableHeader();
         header.addMouseListener(new HeaderListener(
             publicationsTable.getColumnModel(),
             Arrays.asList("Nome", "Gênero", "Álbum", "Artista/Banda", "Popularidade")
         ) {
             @Override
             public void sort(String column) {
                 ((ClientSession) session).sortMyMusics(column);
             }
         });
     }
     
     @Override
     public void buildCatalogTable() {
         JTable catalogTable = ((DialogWithCatalogInterface) component).getCatalogTable();
         final DefaultTableModel model = new ClientCatalogTableModel(catalog);
         catalogTable.setModel(model);
         TableColumn column;
         TableButton button;
         
         column = catalogTable.getColumnModel().getColumn(6);
         button = new TableButton("Comprar");
         button.addTableButtonListener(new TableButtonListener() {
             @Override
             public void tableButtonClicked(int row, int col) {
                 ((ClientSession) session).buy(catalog.get(row));
             }
         });
         column.setCellRenderer(new TableButton("Comprar"));
         column.setCellEditor(button);
         
         JTableHeader header = catalogTable.getTableHeader();
         header.addMouseListener(new HeaderListener(
             catalogTable.getColumnModel(),
             Arrays.asList("Nome", "Gênero", "Álbum", "Artista/Banda", "Popularidade", "Preço")
         ) {
             @Override
             public void sort(String column) {
                 ((UserSession) session).sortCatalog(column);
             }
         });
     }
     
     @Override
     public void buildGreetings() {
         ((DialogWithCatalogInterface) component).getGreetings().setText("Olá "+this.getUser().getName()+", você possui");
         ((DialogWithCreditsInterface) component).getCreditsLabel().setText(this.getClient().getCredits()+" créditos");
     }
     
     @Override
     public void showDialog() {
         ClientFrame clientFrame = new ClientFrame(this);
         this.component = clientFrame;
         this.reloadMyMusic();
         this.buildCatalogTable();
         this.buildGreetings();
         clientFrame.setVisible(true);
     }
  
     public void showAcquireCreditsDialog() {
         AcquireCreditsDialog acquireCreditsDialog = new AcquireCreditsDialog(component, new AcquireCreditsAction(this));
         acquireCreditsDialog.setVisible(true);
     }
     
 }
