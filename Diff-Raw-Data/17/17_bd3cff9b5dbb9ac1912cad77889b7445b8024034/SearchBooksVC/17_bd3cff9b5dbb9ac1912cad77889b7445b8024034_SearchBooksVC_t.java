 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.gubsky.study.elib.vc;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import ru.gubsky.study.elib.models.Book;
 import ru.gubsky.study.elib.models.BookTableModel;
 import ru.gubsky.study.elib.models.ClientModel;
 import ru.gubsky.study.elib.views.ReadBookView;
 
 /**
 
  @author GG
  */
 public class SearchBooksVC extends JFrame
 {
     // use getters, don't use directly
     private ActionListener searchButtonListener_;
     private JTable searchBukzTable_;
     private JButton searchButton_;
     private JTextField searchTf_;
     private ClientModel model_;
     //private JPanel searchPanel_;
     public SearchBooksVC(ClientModel model)
     {
         super();
         model_ = model;
         createGui();
     }
 
     public JTextField getSearchTf()
     {
         if (searchTf_ == null) {
             searchTf_ = new JTextField(30);
         }
         return searchTf_;
     }
 
     public void setSearchButtonListener(ActionListener searchButtonListener)
     {
         getSearchButton().removeActionListener(this.searchButtonListener_);
         this.searchButtonListener_ = searchButtonListener;
         getSearchButton().addActionListener(searchButtonListener);
     }
 
     private JPanel bookPanel_;
     public JPanel getBookPanel()
     {
         if (bookPanel_ == null) {
             bookPanel_ = new JPanel();
             bookPanel_.setLayout(new BorderLayout());
 
             getSearchBukzTable();
         }
         return bookPanel_;
     }
 
     public JTable getSearchBukzTable()
     {
         if (searchBukzTable_ == null) {
             searchBukzTable_ = new JTable();
             JScrollPane scrollPane = new JScrollPane(searchBukzTable_);
             searchBukzTable_.setFillsViewportHeight(true);
 //            getCenterPanel().add(scrollPane);
             getBookPanel().add(scrollPane, BorderLayout.CENTER);
         }
         return searchBukzTable_;
     }
 
     private void createGui()
     {
         setSize(888, 500);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setTitle("eLib. Поиск книг");
 //        setLayout(new BorderLayout(10, 10));
 
         // tab
 //        add(getSearchPanel());
         final JTabbedPane tabbedPane = new JTabbedPane();
         tabbedPane.addTab("Поиск", getSearchPanel());
         tabbedPane.addTab("Жанры", getGenrePanel());
         tabbedPane.addTab("Авторы", getAuthorsPanel());
         tabbedPane.addTab("Популярное", getPopPanel());
         tabbedPane.addTab("Новинки", getNewsPanel());
         tabbedPane.addChangeListener(new ChangeListener()
         {
             @Override
             public void stateChanged(ChangeEvent e)
             {
                 int index = tabbedPane.getSelectedIndex();
//                getSearchBukzTable().getSelectionModel().removeListSelectionListener(listSelectionListener()); // todo: delete
                 removePanel();
                 ArrayList<Book> bukz;
 
                 switch (index) {
                     case 0:
                         getSearchPanel().add(getBookPanel(), BorderLayout.CENTER);
                         search();
                         break;
                     case 1:
                         getGenrePanel().add(getBookPanel(), BorderLayout.CENTER);
                         String[] genres = model_.getGenres();
                         getGenreList().setListData(genres);
                         break;
                     case 2:
                         getAuthorsPanel().add(getBookPanel(), BorderLayout.CENTER);
                         String[] authors = model_.getAuthors();
                         getAuthorsList().setListData(authors);
                         break;
                     case 3:
                         getPopPanel().add(getBookPanel(), BorderLayout.CENTER);
                         updateBooks(model_.getPopBooks());
                         break;
                     case 4:
                         getNewsPanel().add(getBookPanel(), BorderLayout.CENTER);
                         updateBooks(model_.getNewBooks());
                         break;
                 }
//                getSearchBukzTable().getSelectionModel().addListSelectionListener(listSelectionListener()); // todo: delete
 
 
             }
 
         });
 
         add(tabbedPane, BorderLayout.PAGE_START);
 
         getSearchPanel().add(getBookPanel(), BorderLayout.CENTER);
         setSearchButtonListener(searchButtonListener());
     }
 
     private void updateBooks(ArrayList<Book> bukz)
     {
        System.out.println("update books");
         getSearchBukzTable().getSelectionModel().removeListSelectionListener(listSelectionListener());
         BookTableModel btm = new BookTableModel(bukz);
         updateSearch(btm);
         getSearchBukzTable().getSelectionModel().addListSelectionListener(listSelectionListener());
     }
 
     // выбор новой книги в таблице
     private ListSelectionListener listSelListener;
     private ListSelectionListener listSelectionListener()
     {
         if (listSelListener == null) {
             listSelListener = new ListSelectionListener()
             {
                 @Override
                 public void valueChanged(ListSelectionEvent e)
                 {
                     int id = getSelectedBook().id;
                     BookTableModel bm = (BookTableModel) getSearchBukzTable().getModel();
                     getThumbArea().setText(bm.getTextAt(id).substring(0, 150));
                     getOpenBookButton(new ActionListener()
                     {
                         @Override
                         public void actionPerformed(ActionEvent e)
                         {
                             Book selBook = getSelectedBook();
                             model_.addViewForBookId(selBook.id);
                             ReadBookView readBookView = new ReadBookView(selBook);
                             readBookView.setVisible(true);
                         }
 
                     });
                 }
 
             };
         }
         return listSelListener;
     }
 
     // поиск книги
     private ActionListener searchButtonListener()
     {
         ActionListener al = new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 getSearchBukzTable().getSelectionModel().removeListSelectionListener(listSelectionListener());
                 search();
                 getSearchBukzTable().getSelectionModel().addListSelectionListener(listSelectionListener());
             }
 
         };
         return al;
     }
 
     private void search()
     {
         String searchText = getSearchTf().getText();
        updateBooks(model_.getBooksBySearching(searchText));
     }
 
     private void removePanel()
     {
         JPanel panel = getBookPanel();
         if (panel.getParent() != null) {
             panel.getParent().remove(panel);
            //getSearchBukzTable().setModel(new DefaultTableModel());
         }
     }
 
     // ok
     private JButton getSearchButton()
     {
         if (searchButton_ == null) {
             searchButton_ = new JButton("Найти");
         }
         return searchButton_;
     }
 
     private JPanel searchPanel_ = null;
     public JPanel getSearchPanel()
     {
         if (searchPanel_ == null) {
             searchPanel_ = new JPanel();
             searchPanel_.setLayout(new BorderLayout());
             JPanel topPanel = new JPanel();
             topPanel.add(getSearchTf());
             topPanel.add(getSearchButton());
             searchPanel_.add(topPanel, BorderLayout.PAGE_START);
         }
         return searchPanel_;
     }
 
     private JTextArea thumbArea_;
     public JTextArea getThumbArea()
     {
         if (thumbArea_ == null) {
             thumbArea_ = new JTextArea(10, 15);
             thumbArea_.setLineWrap(true);
             thumbArea_.setMaximumSize(new Dimension(350, 200));
             thumbArea_.setWrapStyleWord(true);
             thumbArea_.setEditable(false);
             thumbArea_.setAlignmentY(JComponent.TOP_ALIGNMENT);
             thumbArea_.setAlignmentX(JComponent.CENTER_ALIGNMENT);
             getThumbnailPanel().add(thumbArea_);
         }
         return thumbArea_;
     }
 
     private JPanel thumbnailPanel_;
     private JPanel getThumbnailPanel()
     {
         if (thumbnailPanel_ == null) {
             thumbnailPanel_ = new JPanel();
             thumbnailPanel_.setLayout(new BoxLayout(thumbnailPanel_, BoxLayout.Y_AXIS));
             thumbnailPanel_.setAlignmentY(JComponent.TOP_ALIGNMENT);
             JLabel lbl = new JLabel("Миниатюра");
             lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
             thumbnailPanel_.add(lbl);
 //            getCenterPanel().add(thumbnailPanel_);
             getBookPanel().add(thumbnailPanel_, BorderLayout.EAST);
         }
         return thumbnailPanel_;
     }
 
     /// хз
     public void updateSearch(BookTableModel bookTableModel)
     {
         System.out.println("updateSearch");
         JTable table = getSearchBukzTable();
         System.out.println(bookTableModel.getRowCount());
         table.setModel(bookTableModel);
         table.getSelectionModel().clearSelection();
         table.removeColumn(table.getColumnModel().getColumn(5));
        getThumbArea().setText("");
     }
 
     // ok
     private JButton openBookButton_;
     public JButton getOpenBookButton(ActionListener l)
     {
         if (openBookButton_ == null) {
             openBookButton_ = new JButton("Открыть книгу");
             openBookButton_.addActionListener(l);
             openBookButton_.setAlignmentX(JComponent.CENTER_ALIGNMENT);
             openBookButton_.setAlignmentY(JComponent.TOP_ALIGNMENT);
             getThumbnailPanel().add(openBookButton_);
         }
         return openBookButton_;
     }
 
     // ok
     public Book getSelectedBook()
     {
         BookTableModel bookTableModel = (BookTableModel) getSearchBukzTable().getModel();
         Book book = bookTableModel.getBookAt(getSearchBukzTable().getSelectedRow());
         return book;
     }
 
     private JPanel genrePanel_;
     private JPanel getGenrePanel()
     {
         if (genrePanel_ == null) {
             genrePanel_ = new JPanel();
             genrePanel_.setLayout(new BorderLayout());
             genrePanel_.add(getGenreList(), BorderLayout.LINE_START);
         }
         return genrePanel_;
     }
 
     private JList genreList_;
     private JList getGenreList()
     {
         if (genreList_ == null) {
             genreList_ = new JList();
             genreList_.addListSelectionListener(new ListSelectionListener()
             {
                 @Override
                 public void valueChanged(ListSelectionEvent e)
                 {
                     String genre = (String) genreList_.getSelectedValue();
                     updateBooks(model_.getBooksByGenre(genre));
                 }
 
             });
         }
         return genreList_;
     }
 
     private JPanel authorsPanel_;
     private JPanel getAuthorsPanel()
     {
         if (authorsPanel_ == null) {
             authorsPanel_ = new JPanel();
             authorsPanel_.setLayout(new BorderLayout());
             authorsPanel_.add(getAuthorsList(), BorderLayout.LINE_START);
         }
         return authorsPanel_;
     }
     
     private JList authorsList_;
     private JList getAuthorsList()
     {
         if (authorsList_ == null) {
             authorsList_ = new JList();
             authorsList_.addListSelectionListener(new ListSelectionListener()
             {
                 @Override
                 public void valueChanged(ListSelectionEvent e)
                 {
                     String author = (String) authorsList_.getSelectedValue();
                     updateBooks(model_.getBooksByAuthor(author));
                 }
 
             });
         }
         return authorsList_;
     }
 
     private JPanel popPanel_;
     private JPanel getPopPanel()
     {
         if (popPanel_ == null) {
             popPanel_ = new JPanel();
             popPanel_.setLayout(new BorderLayout());
         }
         return popPanel_;
     }
 
     private JPanel newsPanel_;
     private JPanel getNewsPanel()
     {
         if (newsPanel_ == null) {
             newsPanel_ = new JPanel();
             newsPanel_.setLayout(new BorderLayout());
         }
         return newsPanel_;
     }
 
 }
