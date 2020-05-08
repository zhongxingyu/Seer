 package de.fhkoeln.gm.wba2.phase2.client;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.JButton;
 
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import javax.swing.JTabbedPane;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import de.fhkoeln.gm.wba2.phase2.jaxb.Colour;
 import de.fhkoeln.gm.wba2.phase2.jaxb.ColourPalette;
 import de.fhkoeln.gm.wba2.phase2.jaxb.ColourRef;
 import de.fhkoeln.gm.wba2.phase2.jaxb.FavouriteColour;
 import de.fhkoeln.gm.wba2.phase2.jaxb.FavouriteColourPalette;
 import de.fhkoeln.gm.wba2.phase2.jaxb.Follower;
 import de.fhkoeln.gm.wba2.phase2.jaxb.Ref;
 import de.fhkoeln.gm.wba2.phase2.jaxb.User;
 import de.fhkoeln.gm.wba2.phase2.xmpp_client.ConnectionHandler;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
 import javax.swing.JSlider;
 import javax.swing.JComboBox;
 
 public class ClientFrame extends JFrame {
 
     private static final long serialVersionUID = 1L;
 
     private ConnectionFrame parentFrame;
 
     private JPanel contentPane;
     private ConnectionHandler ch;
     private RESTConnectionHandler rch;
 
     private int newNotificationsNum = 0;
     private int[] create_colour_val = { 0, 0, 0 };
     private String mode;
 
     private JLabel lblStatus;
 
     private List<ColourPalette> newColourpalettesList;
     private JList new_colourpalettes;
     private DefaultListModel<String> newColourpalettesListmodel;
     private DefaultListModel<String> colourListmodel;
     private List<ColourRef> colourList;
     private DefaultListModel<String> colourpaletteListmodel;
     private List<Ref> colourpaletteList;
     private DefaultListModel<String> favColourListmodel;
     private List<FavouriteColour> favColourList;
     private DefaultListModel<String> favColourpaletteListmodel;
     private List<FavouriteColourPalette> favColourpaletteList;
     private List<Ref> userList;
     private DefaultListModel<String> followerListmodel;
     private List<Follower> followerList;
     private DefaultListModel<String> creationsListmodel;
     private List<Ref> creationsList;
     private JTabbedPane tabbedPane;
     private JLabel labelUsername;
     private JButton btnLogout;
     private JList all_colours;
     private JList all_colourpalettes;
     private JPanel colour_view;
     private JPanel[] colourpalette_view;
     private JLabel lblErzeuger;
     private JLabel lblErstelltAm;
     private JLabel outColourCreator;
     private JLabel outColourCreated;
     private JList all_favcolours;
     private JList all_favcolourpalettes;
     private JButton btnFavResource;
     private JButton btnFollowUser;
     private JButton btnUnFavResource;
     private JButton btnUnFollowUser;
     private JPanel p_overview;
     private JPanel p_explore;
     private JPanel p_create;
     private JPanel p_favourites;
     private JPanel p_creations;
     private JPanel p_follower;
     private JSlider red_slider;
     private JSlider green_slider;
     private JSlider blue_slider;
     private JPanel create_colour_view;
     private JPanel[] create_colourpalette_view;
     private List<String> create_colourpalette_colours;
 
     private boolean show_mode_colour = true;
     private JButton btnToPalette;
 
     private JComboBox cb_user;
     private JLabel lblBenutzername;
     private JLabel outBenutzername;
     private JLabel lblBenutzerRegistriertAm;
     private JLabel outBenutzerRegistiertAm;
     private JLabel lblFolgername;
     private JLabel outFolgername;
     private JLabel lblFolgerRegistriertAm;
     private JLabel outFolgerRegistriertAm;
     private JLabel lblFolgerSeit;
     private JLabel outFolgerSeit;
 
     private JComboBox cb_creations_user;
     private JList jl_creations;
 
     public ClientFrame(ConnectionFrame parent) {
         setResizable(false);
         setVisible(true);
         setEnabled(false);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setBounds(100, 100, 800, 600);
         setLocationRelativeTo(null);
 
         parentFrame = parent;
 
         contentPane = new JPanel();
         contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
         setContentPane(contentPane);
         contentPane.setLayout(null);
 
         newColourpalettesListmodel = new DefaultListModel<>();
         colourListmodel = new DefaultListModel<>();
         colourpaletteListmodel = new DefaultListModel<>();
         favColourListmodel = new DefaultListModel<>();
         favColourpaletteListmodel = new DefaultListModel<>();
         followerListmodel = new DefaultListModel<>();
         creationsListmodel = new DefaultListModel<>();
 
         newColourpalettesList = new ArrayList<ColourPalette>();
         colourList = new ArrayList<ColourRef>();
         colourpaletteList = new ArrayList<Ref>();
         favColourList = new ArrayList<FavouriteColour>();
         favColourpaletteList = new ArrayList<FavouriteColourPalette>();
         userList = new ArrayList<Ref>();
         followerList = new ArrayList<>();
         creationsList = new ArrayList<>();
 
         JLabel lblAngemeldetAls = new JLabel("Angemeldet als:");
         lblAngemeldetAls.setBounds(620, 12, 120, 15);
         lblAngemeldetAls.setHorizontalAlignment(SwingConstants.RIGHT);
         contentPane.add(lblAngemeldetAls);
 
         labelUsername = new JLabel("");
         labelUsername.setBounds(380, 33, 360, 15);
         labelUsername.setHorizontalAlignment(SwingConstants.RIGHT);
         contentPane.add(labelUsername);
 
         btnLogout = new JButton("Abmelden");
         btnLogout.setBounds(623, 53, 117, 25);
         btnLogout.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 ch.disconnect();
                 setEnabled(false);
                 parentFrame.setVisible(true);
                 labelUsername.setText("");
                 colourListmodel.clear();
                 colourpaletteListmodel.clear();
                 favColourListmodel.clear();
                 favColourpaletteListmodel.clear();
                 followerListmodel.clear();
             }
         });
         contentPane.add(btnLogout);
 
         tabbedPane = new JTabbedPane(JTabbedPane.TOP);
         tabbedPane.setBounds(55, 118, 500, 408);
         contentPane.add(tabbedPane);
 
         p_overview = new JPanel();
         tabbedPane.addTab("Überblick", null, p_overview, null);
         p_overview.setLayout(null);
 
         tabbedPane.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 updatedTabbedPane();
             }
         });
 
         JScrollPane newColourpalettesSP = new JScrollPane();
         newColourpalettesSP.setBounds(54, 54, 160, 263);
         p_overview.add(newColourpalettesSP);
 
         new_colourpalettes = new JList(newColourpalettesListmodel);
         newColourpalettesSP.setViewportView(new_colourpalettes);
 
         new_colourpalettes
                 .addListSelectionListener(new ListSelectionListener() {
 
                     @Override
                     public void valueChanged(ListSelectionEvent e) {
                         JList list = (JList) e.getSource();
                         int index = list.getSelectedIndex();
 
                         if (index >= 0) {
                             show_mode_colour = false;
 
                             /*
                              * show colourpalette colour views and hide colour
                              * view
                              */
                             for (int i = 0; i < colourpalette_view.length; i++) {
                                 colourpalette_view[i].setVisible(true);
                             }
 
                             colour_view.setVisible(false);
 
                             ColourPalette selected_cp = newColourpalettesList
                                     .get(newColourpalettesList.size() - index
                                             - 1);
 
                             Ref cpref = new Ref();
                             cpref.setRef("/colourpalette/"
                                     + selected_cp.getId().toString());
 
                             ColourPalette cp = rch.getColourPalette(cpref);
 
                             if (cp != null) {
 
                                 /* show creatorname */
                                 User u = rch.getUser(cp.getCreator());
                                 if (u != null) {
                                     outColourCreator.setText(u.getUsername());
                                 } else {
                                     outColourCreator.setText("");
                                 }
 
                                 /* show date of creation */
                                 outColourCreated.setText(getFormattedDate(cp
                                         .getDateOfCreation()));
 
                                 /* set colours */
 
                                 List<ColourRef> crlist = cp.getUsedColours()
                                         .getColour();
 
                                 for (int i = 0; i < colourpalette_view.length; i++) {
                                     if (i < crlist.size()) {
                                         ColourWrapper cw = new ColourWrapper(
                                                 crlist.get(i).getId());
                                         colourpalette_view[i]
                                                 .setBackground(new Color(cw
                                                         .getRed(), cw
                                                         .getGreen(), cw
                                                         .getBlue()));
                                     } else {
                                         colourpalette_view[i]
                                                 .setBackground(contentPane
                                                         .getBackground());
                                     }
                                 }
                             }
                         }
                     }
                 });
 
         JLabel lblNeueFarbpaletten = new JLabel(
                 "Farbpaletten mit Lieblingsfarben oder von gefolgten Benutzern");
         lblNeueFarbpaletten.setBounds(54, 25, 349, 16);
         p_overview.add(lblNeueFarbpaletten);
 
         p_explore = new JPanel();
         tabbedPane.addTab("Entdecken", null, p_explore, null);
         p_explore.setLayout(null);
 
         JScrollPane allColoursSP = new JScrollPane();
         allColoursSP.setBounds(54, 54, 126, 243);
         p_explore.add(allColoursSP);
 
         all_colours = new JList(colourListmodel);
         allColoursSP.setViewportView(all_colours);
 
         all_colours.addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 JList list = (JList) e.getSource();
                 int index = list.getSelectedIndex();
 
                 all_colourpalettes.clearSelection();
 
                 if (index >= 0) {
                     show_mode_colour = true;
 
                     /* hide colourpalette colour views and show colour view */
                     for (int i = 0; i < colourpalette_view.length; i++) {
                         colourpalette_view[i].setVisible(false);
                     }
 
                     colour_view.setVisible(true);
 
                     ColourRef cref = colourList.get(index);
 
                     Colour c = rch.getColour(cref);
 
                     if (c != null) {
                         ColourWrapper cw = new ColourWrapper(c.getId());
 
                         /* show colour */
                         colour_view.setBackground(new Color(cw.getRed(), cw
                                 .getGreen(), cw.getBlue()));
 
                         /* show creatorname */
                         User u = rch.getUser(c.getCreator());
                         if (u != null) {
                             outColourCreator.setText(u.getUsername());
                         } else {
                             outColourCreator.setText("");
                         }
 
                         /* show date of creation */
                         outColourCreated.setText(getFormattedDate(c
                                 .getDateOfCreation()));
                     }
                 }
             }
         });
 
         JLabel lblFarben = new JLabel("Farben");
         lblFarben.setBounds(54, 25, 51, 16);
         p_explore.add(lblFarben);
 
         JScrollPane allColourpalettesSP = new JScrollPane();
         allColourpalettesSP.setBounds(220, 54, 119, 280);
         p_explore.add(allColourpalettesSP);
 
         all_colourpalettes = new JList(colourpaletteListmodel);
         allColourpalettesSP.setViewportView(all_colourpalettes);
 
         all_colourpalettes
                 .addListSelectionListener(new ListSelectionListener() {
 
                     @Override
                     public void valueChanged(ListSelectionEvent e) {
                         JList list = (JList) e.getSource();
                         int index = list.getSelectedIndex();
 
                         all_colours.clearSelection();
 
                         if (index >= 0) {
                             show_mode_colour = false;
 
                             /*
                              * show colourpalette colour views and hide colour
                              * view
                              */
                             for (int i = 0; i < colourpalette_view.length; i++) {
                                 colourpalette_view[i].setVisible(true);
                             }
 
                             colour_view.setVisible(false);
 
                             Ref cpref = colourpaletteList.get(index);
 
                             ColourPalette cp = rch.getColourPalette(cpref);
 
                             if (cp != null) {
 
                                 /* show creatorname */
                                 User u = rch.getUser(cp.getCreator());
                                 if (u != null) {
                                     outColourCreator.setText(u.getUsername());
                                 } else {
                                     outColourCreator.setText("");
                                 }
 
                                 /* show date of creation */
                                 outColourCreated.setText(getFormattedDate(cp
                                         .getDateOfCreation()));
 
                                 /* set colours */
 
                                 List<ColourRef> crlist = cp.getUsedColours()
                                         .getColour();
 
                                 for (int i = 0; i < colourpalette_view.length; i++) {
                                     if (i < crlist.size()) {
                                         ColourWrapper cw = new ColourWrapper(
                                                 crlist.get(i).getId());
                                         colourpalette_view[i]
                                                 .setBackground(new Color(cw
                                                         .getRed(), cw
                                                         .getGreen(), cw
                                                         .getBlue()));
                                     } else {
                                         colourpalette_view[i]
                                                 .setBackground(contentPane
                                                         .getBackground());
                                     }
                                 }
                             }
                         }
                     }
                 });
 
         JLabel lblFarbpaletten = new JLabel("Farbpaletten");
         lblFarbpaletten.setBounds(220, 25, 75, 16);
         p_explore.add(lblFarbpaletten);
 
         btnToPalette = new JButton("Zur Palette");
         btnToPalette.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int index = all_colours.getSelectedIndex();
 
                 if (index >= 0) {
                     String c_code = colourListmodel.get(index);
 
                     ColourWrapper cw = new ColourWrapper(c_code);
                     Color new_c = new Color(cw.getRed(), cw.getGreen(), cw
                             .getBlue());
 
                     red_slider.setValue(cw.getRed());
                     green_slider.setValue(cw.getGreen());
                     blue_slider.setValue(cw.getBlue());
 
                     tabbedPane.setSelectedIndex(2);
                     updatedTabbedPane();
                     // create_colour_view.setBackground(new_c);
                 }
             }
         });
         btnToPalette.setBounds(54, 306, 126, 28);
         p_explore.add(btnToPalette);
 
         colour_view = new JPanel();
         colour_view.setBounds(623, 150, 117, 60);
         contentPane.add(colour_view);
 
         lblErzeuger = new JLabel("Erzeuger");
         lblErzeuger.setBounds(623, 239, 117, 16);
         lblErzeuger.setVisible(false);
         contentPane.add(lblErzeuger);
 
         outColourCreator = new JLabel("");
         outColourCreator.setBounds(623, 259, 117, 16);
         contentPane.add(outColourCreator);
 
         lblErstelltAm = new JLabel("Erstellt am");
         lblErstelltAm.setBounds(623, 295, 117, 16);
         lblErstelltAm.setVisible(false);
         contentPane.add(lblErstelltAm);
 
         outColourCreated = new JLabel("");
         outColourCreated.setBounds(623, 318, 117, 16);
         contentPane.add(outColourCreated);
 
         btnFavResource = new JButton("Setze als Favorit");
         btnFavResource.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
 
                 if (show_mode_colour) {
                     /* fav colour */
                     int index = all_colours.getSelectedIndex();
 
                     if (index >= 0) {
                         String ccode = colourListmodel.get(index);
 
                         for (ColourRef cref : favColourList) {
                             if (cref.getId().equalsIgnoreCase(ccode)) {
                                 lblStatus
                                         .setText("Farbe ist bereits eine Lieblingsfarbe!");
                                 return;
                             }
                         }
 
                         if (ch.subscribeToNode(ccode)
                                 && rch.setColourFavourite(ccode)) {
                             lblStatus.setText("\"" + ccode
                                     + "\" wurde als Lieblingsfarbe gesetzt!");
                             refreshFavouriteList();
                         } else {
                             lblStatus
                                     .setText("Farbe konnte nicht als Lieblingsfarbe gesetzt werden!");
                         }
                     }
                 } else {
 
                     if (mode == "explore") {
 
                         /* fav colourpalette */
                         int index = all_colourpalettes.getSelectedIndex();
 
                         if (index >= 0) {
                             String cpid = colourpaletteList.get(index).getId()
                                     .toString();
 
                             for (Ref cref : favColourpaletteList) {
                                 if (cref.getId().toString()
                                         .equalsIgnoreCase(cpid)) {
                                     lblStatus
                                             .setText("Farbpalette ist bereits als Lieblingspalette festgelegt!");
                                     return;
                                 }
                             }
 
                             if (rch.setColourPaletteFavourite(cpid)) {
                                 refreshFavouriteList();
                                 lblStatus
                                         .setText("\""
                                                 + cpid
                                                 + "\" wurde als neue Lieblinsgfabplatte hinzugefügt!");
                             } else {
                                 lblStatus
                                         .setText("Farbpalette konnte nicht als Lieblingsfarbplaette hinzugefügt werden!");
                             }
                         }
                     } else if (mode == "creations") {
                         /* fav colourpalette */
                         int index = jl_creations.getSelectedIndex();
 
                         if (index >= 0) {
                             String cpid = creationsList.get(index).getId()
                                     .toString();
 
                             for (Ref cref : favColourpaletteList) {
                                 if (cref.getId().toString()
                                         .equalsIgnoreCase(cpid)) {
                                     lblStatus
                                             .setText("Farbpalette ist bereits als Lieblingspalette festgelegt!!");
                                     return;
                                 }
                             }
 
                             if (rch.setColourPaletteFavourite(cpid)) {
                                 refreshFavouriteList();
                                 lblStatus
                                         .setText("\""
                                                 + cpid
                                                 + "\" wurde als neue Lieblinsgfabplatte hinzugefügt!");
                             } else {
                                 lblStatus
                                         .setText("Farbpalette konnte nicht als Lieblingsfarbplaette hinzugefügt werden!");
                             }
                         }
                     }
                 }
             }
         });
         btnFavResource.setBounds(623, 406, 117, 28);
         btnFavResource.setVisible(false);
         contentPane.add(btnFavResource);
 
         btnUnFavResource = new JButton("Lösche Favorit");
         btnUnFavResource.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
 
                 if (show_mode_colour) {
                     /* fav colour */
                     int index = all_favcolours.getSelectedIndex();
 
                     if (index >= 0) {
                         String ccode = favColourListmodel.get(index);
 
                         if (ch.unsubscribeToNode(ccode)
                                 && rch.unsetColourFavourite(ccode)) {
                             lblStatus.setText("\"" + ccode
                                     + "\" ist keine Lieblingsfarbe mehr!");
                             refreshFavouriteList();
                         } else {
                             lblStatus.setText("\"" + ccode
                                     + "\" ist immer noch eine Lieblingsfarbe!");
                         }
                     }
                 } else {
                     /* fav colourpalette */
                     int index = all_favcolourpalettes.getSelectedIndex();
 
                     if (index >= 0) {
                         String cpid = favColourpaletteList.get(index).getId()
                                 .toString();
 
                         if (rch.unsetColourPaletteFavourite(cpid)) {
                             refreshFavouriteList();
                             lblStatus
                                     .setText("\""
                                             + cpid
                                             + "\" wurde als Lieblingsfarbpalette hizugefügt!");
                         } else {
                             lblStatus
                                     .setText("Farbpalette konnte nicht als Lieblingsfarbplaette hinzugefügt werden!");
                         }
                     }
                 }
             }
         });
         btnUnFavResource.setBounds(623, 406, 117, 28);
         btnUnFavResource.setVisible(false);
         contentPane.add(btnUnFavResource);
 
         btnFollowUser = new JButton("Benutzer folgen");
         btnFollowUser.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
 
                 int index = -1;
                 User u = null;
 
                 if (show_mode_colour) {
                     index = all_colours.getSelectedIndex();
 
                     if (index >= 0) {
                         u = rch.getCreatorOfColour(colourList.get(index));
                     }
                 } else {
 
                     if (mode == "explore") {
                         index = all_colourpalettes.getSelectedIndex();
 
                         if (index >= 0) {
                             u = rch.getCreatorOfColourpalette(colourpaletteList
                                     .get(index));
                         }
                     } else if (mode == "creations") {
                         index = cb_creations_user.getSelectedIndex();
 
                         if (index >= 0) {
                             u = rch.getCreatorOfColourpalette(userList
                                     .get(index));
                         }
                     }
                 }
 
                 if (u == null)
                     return;
 
                 if (ch.subscribeToNode("user" + u.getId().toString())
                         && rch.followUser(u)) {
                     lblStatus.setText("User \"" + u.getUsername()
                             + "\" wird nun gefplgt!");
                 } else {
                     lblStatus.setText("User \"" + u.getUsername()
                             + "\" wird bereits gefolgt!");
                 }
 
             }
         });
         btnFollowUser.setBounds(623, 446, 117, 28);
         btnFollowUser.setVisible(false);
         contentPane.add(btnFollowUser);
 
         btnUnFollowUser = new JButton("Benutzer lösen");
         btnUnFollowUser.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
 
                 int index = -1;
                 User u = null;
 
                 if (show_mode_colour) {
                     index = all_favcolours.getSelectedIndex();
 
                     if (index >= 0) {
                         u = rch.getCreatorOfColour(favColourList.get(index));
                     }
                 } else {
                     index = all_favcolourpalettes.getSelectedIndex();
 
                     if (index >= 0) {
                         u = rch.getCreatorOfColourpalette(favColourpaletteList
                                 .get(index));
                     }
                 }
 
                 if (u == null)
                     return;
 
                 if (ch.unsubscribeToNode("user" + u.getId().toString())
                         && rch.unfollowUser(u)) {
                     lblStatus.setText("User \"" + u.getUsername()
                             + "\" wird gefolgt!");
                 } else {
                     lblStatus.setText("User \"" + u.getUsername()
                             + "\" wird bereits gefolgt!");
                 }
 
             }
         });
         btnUnFollowUser.setBounds(623, 446, 117, 28);
         btnUnFollowUser.setVisible(false);
         contentPane.add(btnUnFollowUser);
 
         colourpalette_view = new JPanel[8];
         for (int i = 0; i < colourpalette_view.length; i++) {
             colourpalette_view[i] = new JPanel();
             colourpalette_view[i].setBounds(623 + i * 18, 150, 15, 60);
             contentPane.add(colourpalette_view[i]);
         }
 
         p_create = new JPanel();
         tabbedPane.addTab("Erstellen", null, p_create, null);
         p_create.setLayout(null);
 
         red_slider = new JSlider(0, 255);
         red_slider.setValue(0);
         red_slider.setBounds(93, 12, 331, 43);
         red_slider.addChangeListener(new ChangeListener() {
 
             @Override
             public void stateChanged(ChangeEvent arg) {
                 create_colour_val[0] = ((JSlider) arg.getSource()).getValue();
                 create_colour_view.setBackground(new Color(
                         create_colour_val[0], create_colour_val[1],
                         create_colour_val[2]));
             }
         });
         p_create.add(red_slider);
 
         JLabel lblRot = new JLabel("Rot");
         lblRot.setBounds(30, 27, 51, 16);
         p_create.add(lblRot);
 
         green_slider = new JSlider(0, 255);
         green_slider.setValue(0);
         green_slider.setBounds(93, 67, 331, 43);
         green_slider.addChangeListener(new ChangeListener() {
 
             @Override
             public void stateChanged(ChangeEvent arg) {
                 create_colour_val[1] = ((JSlider) arg.getSource()).getValue();
                 create_colour_view.setBackground(new Color(
                         create_colour_val[0], create_colour_val[1],
                         create_colour_val[2]));
             }
         });
         p_create.add(green_slider);
 
         JLabel lblGrn = new JLabel("Grün");
         lblGrn.setBounds(30, 85, 51, 16);
         p_create.add(lblGrn);
 
         blue_slider = new JSlider(0, 255);
         blue_slider.setValue(0);
         blue_slider.setBounds(93, 122, 331, 43);
         blue_slider.addChangeListener(new ChangeListener() {
 
             @Override
             public void stateChanged(ChangeEvent arg) {
                 create_colour_val[2] = ((JSlider) arg.getSource()).getValue();
                 create_colour_view.setBackground(new Color(
                         create_colour_val[0], create_colour_val[1],
                         create_colour_val[2]));
             }
         });
         p_create.add(blue_slider);
 
         JLabel lblBlau = new JLabel("Blau");
         lblBlau.setBounds(30, 138, 51, 16);
         p_create.add(lblBlau);
 
         create_colour_view = new JPanel();
         create_colour_view.setBounds(30, 187, 111, 115);
         create_colour_view.setBackground(new Color(0, 0, 0));
         p_create.add(create_colour_view);
 
         JButton btnCreateColour = new JButton("Erzeuge Farbe");
         btnCreateColour.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
                 ColourWrapper cw = new ColourWrapper(create_colour_val[0],
                         create_colour_val[1], create_colour_val[2]);
                 String new_colour = cw.getHexCode();
 
                 if (rch.createColour(new_colour)) {
                     lblStatus.setText("\"" + new_colour + "\" wurde erzeugt!");
                     refreshBaseList();
                 } else {
                     lblStatus.setText("\"" + new_colour
                             + "\" konnte nicht erzeugt werden!");
                 }
             }
         });
         btnCreateColour.setBounds(30, 314, 111, 28);
         p_create.add(btnCreateColour);
 
         JButton btnAddColour = new JButton(">");
         btnAddColour.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
 
                 if (create_colourpalette_colours == null) {
                     create_colourpalette_colours = new ArrayList<String>();
                 }
 
                 if (create_colourpalette_colours.size() == 8) {
                     lblStatus
                             .setText("Es sind keine weiteren Farben in einer Farbpalette erlaubt!");
                     return;
                 }
 
                 String c_code = new ColourWrapper(create_colour_val[0],
                         create_colour_val[1], create_colour_val[2])
                         .getHexCode();
 
                 for (int i = 0; i < create_colourpalette_colours.size(); i++) {
                     if (create_colourpalette_colours.get(i).equalsIgnoreCase(
                             c_code)) {
                         lblStatus.setText("\"" + c_code
                                 + "\" existiert bereits in der Farbpalette!");
                         return;
                     }
                 }
 
                 create_colourpalette_colours.add(c_code);
 
                 int new_pos = create_colourpalette_colours.size();
                 Color new_c = new Color(create_colour_val[0],
                         create_colour_val[1], create_colour_val[2]);
                 create_colourpalette_view[new_pos - 1].setBackground(new_c);
             }
         });
         btnAddColour.setBounds(166, 187, 32, 115);
         p_create.add(btnAddColour);
 
         create_colourpalette_view = new JPanel[8];
         for (int i = 0; i < create_colourpalette_view.length; i++) {
             create_colourpalette_view[i] = new JPanel();
             create_colourpalette_view[i].setBounds(217 + i * 28, 187, 25, 115);
             p_create.add(create_colourpalette_view[i]);
         }
 
         JButton btnResetColourpalette = new JButton("Reset");
         btnResetColourpalette.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 create_colourpalette_colours = null;
                 for (int i = 0; i < create_colourpalette_view.length; i++) {
                     create_colourpalette_view[i].setBackground(contentPane
                             .getBackground());
                 }
             }
         });
         btnResetColourpalette.setBounds(217, 314, 111, 28);
         p_create.add(btnResetColourpalette);
 
         JButton btnCreateColourpalette = new JButton("Erzeuge Palette");
         btnCreateColourpalette.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (create_colourpalette_colours.size() < 2) {
                     lblStatus
                             .setText("Ein Farbpalette muss aus mindestens 2 Farben bestehen!");
                     return;
                 }
 
                 String location = rch
                         .createColourpalette(create_colourpalette_colours);
                 if (location != null) {
                     lblStatus.setText("Farbpalette wurde erzeugt!");
 
                     Ref cpref = new Ref();
                     cpref.setRef(location);
 
                     ColourPalette cp = rch.getColourPalette(cpref);
 
                     if (cp != null) {
 
                         String marshalled_cp = JAXBTools.marshall(cp);
 
                         // ugly
                         marshalled_cp = marshalled_cp.substring(marshalled_cp
                                 .indexOf("\n"));
 
                         /* publish to colourcode-topics */
                         for (String c : create_colourpalette_colours) {
                             if (!ch.publishWithPayload(c, marshalled_cp)) {
                                 lblStatus
                                         .setText("Konnte nicht unter den Farbcodes veröffentlichen!");
                                 return;
                             }
                         }
 
                         /* publish to creator */
                         if (!ch.publishWithPayload("user"
                                 + cp.getCreator().getId().toString(),
                                 marshalled_cp)) {
                             lblStatus
                                     .setText("Konnte nicht unter einem Benutzernamen veröffentllichen!");
                             return;
                         }
 
                         lblStatus.setText(lblStatus.getText()
                                 + " Das Veröffentlichen war erfolgreich!");
                     }
 
                     create_colourpalette_colours.clear();
                     for (int i = 0; i < create_colourpalette_view.length; i++) {
                         create_colourpalette_view[i].setBackground(contentPane
                                 .getBackground());
                     }
 
                     refreshBaseList();
                 } else {
                     lblStatus
                             .setText("Farbpalette konnte nicht erzeugt werden!");
                 }
             }
         });
         btnCreateColourpalette.setBounds(340, 314, 111, 28);
         p_create.add(btnCreateColourpalette);
 
         p_favourites = new JPanel();
         tabbedPane.addTab("Favoriten", null, p_favourites, null);
         p_favourites.setLayout(null);
 
         JLabel lblFavFarben = new JLabel("Farben");
         lblFavFarben.setBounds(54, 25, 51, 16);
         p_favourites.add(lblFavFarben);
 
         JScrollPane allFavColoursSP = new JScrollPane();
         allFavColoursSP.setBounds(54, 54, 126, 280);
         p_favourites.add(allFavColoursSP);
 
         all_favcolours = new JList(favColourListmodel);
         allFavColoursSP.setViewportView(all_favcolours);
 
         all_favcolours.addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 JList list = (JList) e.getSource();
                 int index = list.getSelectedIndex();
 
                 all_favcolourpalettes.clearSelection();
 
                 if (index >= 0) {
                     show_mode_colour = true;
 
                     /* hide colourpalette colour views and show colour view */
                     for (int i = 0; i < colourpalette_view.length; i++) {
                         colourpalette_view[i].setVisible(false);
                     }
 
                     colour_view.setVisible(true);
 
                     ColourRef cref = favColourList.get(index);
 
                     Colour c = rch.getColour(cref);
 
                     if (c != null) {
                         ColourWrapper cw = new ColourWrapper(c.getId());
 
                         /* show colour */
                         colour_view.setBackground(new Color(cw.getRed(), cw
                                 .getGreen(), cw.getBlue()));
 
                         /* show creatorname */
                         User u = rch.getUser(c.getCreator());
                         if (u != null) {
                             outColourCreator.setText(u.getUsername());
                         } else {
                             outColourCreator.setText("");
                         }
 
                         /* show date of creation */
                         outColourCreated.setText(getFormattedDate(c
                                 .getDateOfCreation()));
                     }
                 }
             }
         });
 
         JLabel lblFavFarbpaletten = new JLabel("Farbpaletten");
         lblFavFarbpaletten.setBounds(220, 25, 75, 16);
         p_favourites.add(lblFavFarbpaletten);
 
         JScrollPane allFavColourpalettesSP = new JScrollPane();
         allFavColourpalettesSP.setBounds(220, 54, 119, 280);
         p_favourites.add(allFavColourpalettesSP);
 
         all_favcolourpalettes = new JList(favColourpaletteListmodel);
         allFavColourpalettesSP.setViewportView(all_favcolourpalettes);
 
         all_favcolourpalettes
                 .addListSelectionListener(new ListSelectionListener() {
 
                     @Override
                     public void valueChanged(ListSelectionEvent e) {
                         JList list = (JList) e.getSource();
                         int index = list.getSelectedIndex();
 
                         all_favcolours.clearSelection();
 
                         if (index >= 0) {
                             show_mode_colour = false;
 
                             /*
                              * show colourpalette colour views and hide colour
                              * view
                              */
                             for (int i = 0; i < colourpalette_view.length; i++) {
                                 colourpalette_view[i].setVisible(true);
                             }
 
                             colour_view.setVisible(false);
 
                             Ref cpref = favColourpaletteList.get(index);
 
                             ColourPalette cp = rch.getColourPalette(cpref);
 
                             if (cp != null) {
 
                                 /* show creatorname */
                                 User u = rch.getUser(cp.getCreator());
                                 if (u != null) {
                                     outColourCreator.setText(u.getUsername());
                                 } else {
                                     outColourCreator.setText("");
                                 }
 
                                 /* show date of creation */
                                 outColourCreated.setText(getFormattedDate(cp
                                         .getDateOfCreation()));
 
                                 /* set colours */
 
                                 List<ColourRef> crlist = cp.getUsedColours()
                                         .getColour();
 
                                 for (int i = 0; i < colourpalette_view.length; i++) {
                                     if (i < crlist.size()) {
                                         ColourWrapper cw = new ColourWrapper(
                                                 crlist.get(i).getId());
                                         colourpalette_view[i]
                                                 .setBackground(new Color(cw
                                                         .getRed(), cw
                                                         .getGreen(), cw
                                                         .getBlue()));
                                     } else {
                                         colourpalette_view[i]
                                                 .setBackground(contentPane
                                                         .getBackground());
                                     }
                                 }
                             }
                         }
                     }
                 });
 
         p_creations = new JPanel();
         tabbedPane.addTab("Erzeugnisse", null, p_creations, null);
         p_creations.setLayout(null);
 
         JLabel lblBenutzerCreations = new JLabel("Benutzer");
         lblBenutzerCreations.setBounds(54, 25, 51, 16);
         p_creations.add(lblBenutzerCreations);
 
         cb_creations_user = new JComboBox();
         cb_creations_user.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
 
                 int index = ((JComboBox) arg.getSource()).getSelectedIndex();
 
                 if (index >= 0) {
                     refreshCreationsList();
                 }
             }
         });
         cb_creations_user.setBounds(54, 50, 132, 28);
         p_creations.add(cb_creations_user);
 
         JScrollPane scrollPane1 = new JScrollPane();
         scrollPane1.setBounds(54, 118, 132, 224);
         p_creations.add(scrollPane1);
 
         jl_creations = new JList(creationsListmodel);
         jl_creations.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent arg) {
 
                 int index = ((JList) arg.getSource()).getSelectedIndex();
 
                 if (index >= 0) {
                     show_mode_colour = false;
 
                     /* show colourpalette colour views and hide colour view */
                     for (int i = 0; i < colourpalette_view.length; i++) {
                         colourpalette_view[i].setVisible(true);
                     }
 
                     colour_view.setVisible(false);
 
                     Ref cpref = creationsList.get(index);
 
                     ColourPalette cp = rch.getColourPalette(cpref);
 
                     if (cp != null) {
 
                         /* show creatorname */
                         int index_user = cb_creations_user.getSelectedIndex();
                         outColourCreator.setText((String) cb_creations_user
                                 .getItemAt(index_user));
 
                         /* show date of creation */
                         outColourCreated.setText(getFormattedDate(cp
                                 .getDateOfCreation()));
 
                         /* set colours */
 
                         List<ColourRef> crlist = cp.getUsedColours()
                                 .getColour();
 
                         for (int i = 0; i < colourpalette_view.length; i++) {
                             if (i < crlist.size()) {
                                 ColourWrapper cw = new ColourWrapper(crlist
                                         .get(i).getId());
                                 colourpalette_view[i].setBackground(new Color(
                                         cw.getRed(), cw.getGreen(), cw
                                                 .getBlue()));
                             } else {
                                 colourpalette_view[i].setBackground(contentPane
                                         .getBackground());
                             }
                         }
                     }
                 }
             }
         });
         scrollPane1.setViewportView(jl_creations);
 
         p_follower = new JPanel();
         tabbedPane.addTab("Folger", null, p_follower, null);
         p_follower.setLayout(null);
 
         JScrollPane scrollPane = new JScrollPane();
         scrollPane.setBounds(54, 118, 132, 224);
         p_follower.add(scrollPane);
 
         JList jl_follower = new JList(followerListmodel);
         jl_follower.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent arg) {
 
                 int index = ((JList) arg.getSource()).getSelectedIndex();
 
                 if (index >= 0) {
                     Follower f = followerList.get(index);
                     User u = rch.getUser(f);
 
                     if (u != null) {
 
                         lblFolgername.setVisible(true);
                         outFolgername.setText(u.getUsername());
                         outFolgername.setVisible(true);
 
                         lblFolgerRegistriertAm.setVisible(true);
                         outFolgerRegistriertAm.setText(getFormattedDate(u
                                 .getDateOfRegistration()));
                         outFolgerRegistriertAm.setVisible(true);
 
                         lblFolgerSeit.setVisible(true);
                         outFolgerSeit.setText(getFormattedDate(f
                                 .getFollowingSince()));
                         outFolgerSeit.setVisible(true);
                     }
                 }
             }
         });
         scrollPane.setViewportView(jl_follower);
 
         cb_user = new JComboBox();
         cb_user.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg) {
 
                 int index = ((JComboBox) arg.getSource()).getSelectedIndex();
 
                 if (index >= 0) {
                     User u = rch.getUser(userList.get(index));
 
                     if (u != null) {
 
                         outBenutzername.setText(u.getUsername());
 
                         /* show date of creation */
                         outBenutzerRegistiertAm.setText(getFormattedDate(u
                                 .getDateOfRegistration()));
 
                         lblFolgername.setVisible(false);
                         outFolgername.setText("");
                         lblFolgerRegistriertAm.setVisible(false);
                         outFolgerRegistriertAm.setText("");
                         lblFolgerSeit.setVisible(false);
                         outFolgerSeit.setText("");
 
                         refreshFollowerList();
                     }
                 }
             }
         });
         cb_user.setBounds(54, 50, 132, 28);
         p_follower.add(cb_user);
 
         JLabel lblFolger = new JLabel("Folger");
         lblFolger.setBounds(54, 90, 51, 16);
         p_follower.add(lblFolger);
 
         JLabel lblBenutzer = new JLabel("Benutzer");
         lblBenutzer.setBounds(54, 25, 51, 16);
         p_follower.add(lblBenutzer);
 
         lblBenutzername = new JLabel("Benutzername");
         lblBenutzername.setBounds(223, 56, 90, 16);
         lblBenutzername.setVisible(false);
         p_follower.add(lblBenutzername);
 
         outBenutzername = new JLabel("");
         outBenutzername.setBounds(223, 72, 145, 16);
         outBenutzername.setVisible(false);
         p_follower.add(outBenutzername);
 
         lblBenutzerRegistriertAm = new JLabel("Registriert am");
         lblBenutzerRegistriertAm.setBounds(223, 101, 90, 16);
         lblBenutzerRegistriertAm.setVisible(false);
         p_follower.add(lblBenutzerRegistriertAm);
 
         outBenutzerRegistiertAm = new JLabel("");
         outBenutzerRegistiertAm.setBounds(223, 118, 145, 16);
         outBenutzerRegistiertAm.setVisible(false);
         p_follower.add(outBenutzerRegistiertAm);
 
         lblFolgername = new JLabel("Folgername");
         lblFolgername.setBounds(223, 170, 72, 16);
         lblFolgername.setVisible(false);
         p_follower.add(lblFolgername);
 
         outFolgername = new JLabel("");
         outFolgername.setBounds(223, 187, 145, 16);
         outFolgername.setVisible(false);
         p_follower.add(outFolgername);
 
         lblFolgerRegistriertAm = new JLabel("Registriert am");
         lblFolgerRegistriertAm.setBounds(223, 215, 90, 16);
         lblFolgerRegistriertAm.setVisible(false);
         p_follower.add(lblFolgerRegistriertAm);
 
         outFolgerRegistriertAm = new JLabel("");
         outFolgerRegistriertAm.setBounds(223, 236, 145, 16);
         outFolgerRegistriertAm.setVisible(false);
         p_follower.add(outFolgerRegistriertAm);
 
         lblFolgerSeit = new JLabel("Folger seit");
         lblFolgerSeit.setBounds(223, 264, 72, 16);
         lblFolgerSeit.setVisible(false);
         p_follower.add(lblFolgerSeit);
 
         outFolgerSeit = new JLabel("");
         outFolgerSeit.setBounds(223, 287, 145, 16);
         outFolgerSeit.setVisible(false);
         p_follower.add(outFolgerSeit);
 
         lblStatus = new JLabel("Ready!");
         lblStatus.setBounds(60, 540, 680, 16);
         contentPane.add(lblStatus);
 
     }
 
     private void refreshBaseList() {
         colourList.clear();
         colourpaletteList.clear();
         colourListmodel.clear();
         colourpaletteListmodel.clear();
 
         colourList = rch.getAllColours();
         for (ColourRef curr_c : colourList) {
             colourListmodel.addElement(curr_c.getId());
         }
 
         colourpaletteList = rch.getAllColourpalettes();
         for (Ref curr_cp : colourpaletteList) {
             colourpaletteListmodel.addElement(curr_cp.getRef());
         }
     }
 
     private void refreshFavouriteList() {
 
         favColourList.clear();
         favColourpaletteList.clear();
         favColourListmodel.clear();
         favColourpaletteListmodel.clear();
 
         favColourList = rch.getAllFavColours();
         for (FavouriteColour curr_fc : favColourList) {
             favColourListmodel.addElement(curr_fc.getId());
         }
 
         favColourpaletteList = rch.getAllFavColourpalettes();
         for (FavouriteColourPalette curr_fcp : favColourpaletteList) {
             favColourpaletteListmodel.addElement(curr_fcp.getRef());
         }
     }
 
     private void refreshCreationsList() {
         creationsList.clear();
         creationsListmodel.clear();
 
         int index = cb_creations_user.getSelectedIndex();
 
         if (index >= 0) {
 
             Ref uref = userList.get(index);
 
             creationsList = rch.getAllUserCreations(uref);
             for (Ref curr_creation : creationsList) {
                 creationsListmodel.addElement(curr_creation.getRef());
             }
         }
     }
 
     private void refreshFollowerList() {
         followerList.clear();
         followerListmodel.clear();
 
         int index = cb_user.getSelectedIndex();
 
         if (index >= 0) {
             Ref uref = userList.get(index);
 
             followerList = rch.getAllFollowerForAUser(uref);
             for (Follower curr_f : followerList) {
                 User u = rch.getUser(curr_f);
 
                 if (u != null)
                     followerListmodel.addElement(u.getUsername());
                 else
                     followerListmodel.addElement(curr_f.getRef());
             }
         }
     }
 
     private void refreshUserList() {
         userList.clear();
        cb_user.removeAll();
        cb_creations_user.removeAll();
 
         userList = rch.getAllUsers();
 
         for (Ref uref : userList) {
             User u = rch.getUser(uref);
 
             if (u != null) {
                 cb_user.addItem(u.getUsername());
                 cb_creations_user.addItem(u.getUsername());
             } else {
                 cb_user.addItem(uref.getId());
                 cb_creations_user.addItem(uref.getRef());
             }
 
         }
     }
 
     private void refreshLists() {
         /* Fill listmodel with all existing resources */
         // parentFrame.setVisible(true);
 
         refreshBaseList();
         refreshFavouriteList();
         refreshCreationsList();
         refreshUserList();
         refreshFollowerList();
     }
 
     private void refreshNewColourpalettes() {
 
         newColourpalettesListmodel.removeAllElements();
 
         for (ColourPalette cp : newColourpalettesList) {
             newColourpalettesListmodel.add(0, "/colourpalette/" + cp.getId());
         }
 
         refreshLists();
     }
 
     public void receivedNewCPNotification(ColourPalette cp) {
 
         newNotificationsNum += 1;
 
         String overview_name = tabbedPane.getTitleAt(0);
 
         int index = overview_name.indexOf("(");
 
         if (index > 0) {
            overview_name = overview_name.substring(0, index - 2);
         }
 
         tabbedPane.setTitleAt(0, overview_name + " (" + newNotificationsNum
                 + ")");
 
         newColourpalettesList.add(cp);
         refreshNewColourpalettes();
         lblStatus
                 .setText("Eine neue Farbpalette mit einer deiner Lieblingsfarben wurde erzeugt, oder von jemanden den du folgst!");
     }
 
     public void receivedNewUNotification(ColourPalette cp) {
         System.out.println("USER - " + cp.getCreator().getId().toString());
     }
 
     private void updatedTabbedPane() {
         JPanel chosen = (JPanel) tabbedPane.getComponentAt(tabbedPane
                 .getSelectedIndex());
 
         colour_view.setVisible(false);
 
         for (int i = 0; i < colourpalette_view.length; i++) {
             colourpalette_view[i].setVisible(false);
         }
 
         outColourCreator.setText("");
         outColourCreated.setText("");
 
         new_colourpalettes.clearSelection();
         all_colours.clearSelection();
         all_colourpalettes.clearSelection();
         all_favcolours.clearSelection();
         all_favcolourpalettes.clearSelection();
 
         boolean overview_mode = false;
         boolean colour_mode = false;
         boolean favourite_mode = false;
 
         if (chosen.equals(p_overview)) {
             mode = "overview";
             colour_mode = false;
             favourite_mode = false;
 
             newNotificationsNum = 0;
             String overview_name = tabbedPane.getTitleAt(tabbedPane
                     .getSelectedIndex());
 
             int index = overview_name.indexOf("(");
 
             if (index > 0) {
                 overview_name = overview_name.substring(0, index - 1);
             }
 
             tabbedPane.setTitleAt(0, overview_name);
             overview_mode = true;
         } else if (chosen.equals(p_explore)) {
             mode = "explore";
             colour_mode = true;
             favourite_mode = false;
         } else if (chosen.equals(p_favourites)) {
             mode = "favourites";
             colour_mode = false;
             favourite_mode = true;
         } else if (chosen.equals(p_follower)) {
             mode = "follower";
             lblBenutzername.setVisible(true);
             outBenutzername.setVisible(true);
             lblBenutzerRegistriertAm.setVisible(true);
             outBenutzerRegistiertAm.setVisible(true);
         } else if (chosen.equals(p_creations)) {
             mode = "creations";
             colour_mode = true;
             favourite_mode = false;
         }
 
         btnFavResource.setVisible(colour_mode);
         btnFollowUser.setVisible(colour_mode);
         btnUnFavResource.setVisible(favourite_mode);
         btnUnFollowUser.setVisible(favourite_mode);
 
         lblErzeuger.setVisible(overview_mode || colour_mode || favourite_mode);
         lblErstelltAm
                 .setVisible(overview_mode || colour_mode || favourite_mode);
     }
 
     private String getFormattedDate(XMLGregorianCalendar xgc) {
 
         if (xgc == null)
             return "";
 
         String date = ((xgc.getDay() < 10) ? "0" + xgc.getDay() : xgc.getDay())
                 + "."
                 + ((xgc.getMonth() < 10) ? "0" + xgc.getMonth() : xgc
                         .getMonth())
                 + "."
                 + xgc.getYear()
                 + " - "
                 + ((xgc.getHour() < 10) ? "0" + xgc.getHour() : xgc.getHour())
                 + ":"
                 + ((xgc.getMinute() < 10) ? "0" + xgc.getMinute() : xgc
                         .getMinute());
         return date;
     }
 
     public void setConnectionHandlers(ConnectionHandler ch,
             RESTConnectionHandler rch) {
         this.ch = ch;
         this.rch = rch;
 
         labelUsername.setText(ch.getUsername() + "@" + Config.hostname);
 
         refreshLists();
 
         this.ch.addItemListener(new ItemLoggingListener(this));
     }
 }
