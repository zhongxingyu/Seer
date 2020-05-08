 /*
  * Copyright (c) 2009 by Samuel Berner (samuel.berner@gmail.com), all rights reserved
  * Created on 22.08.2009
  */
 package figurabia.ui;
 
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import com.xuggle.ferry.JNIMemoryManager;
 import com.xuggle.ferry.JNIMemoryManager.MemoryModel;
 
 import exmoplay.engine.MediaPlayer;
 import figurabia.domain.Figure;
 import figurabia.framework.FigureModel;
 import figurabia.framework.FigurePositionListener;
 import figurabia.io.BeatPictureCache;
 import figurabia.io.FigureStore;
 import figurabia.io.FiguresTreeStore;
 import figurabia.io.VideoDir;
 import figurabia.io.VideoMetaData;
 import figurabia.io.VideoMetaDataStore;
 import figurabia.io.workspace.LocalFileWorkspace;
 import figurabia.io.workspace.Workspace;
 import figurabia.service.FigureCreationService;
 import figurabia.service.FigureUpdateService;
 import figurabia.ui.figureeditor.FigureEditPerspective;
 import figurabia.ui.figureexplorer.FigureExplorerPerspective;
 import figurabia.ui.figuremapper.FigureMapperPerspective;
 import figurabia.ui.framework.Perspective;
 
 @SuppressWarnings("serial")
 public class ApplicationFrame extends JFrame {
 
     private final static int INITIAL_WIDTH = 1000;
     private final static int INITIAL_HEIGHT = 750;
 
     private Workspace workspace;
 
     private final FigureStore figureStore;
     private final FiguresTreeStore treeStore;
     private final BeatPictureCache beatPictureCache;
     private final VideoMetaDataStore videoMetaDataStore;
     private final VideoDir videoDir;
 
     private final FigureCreationService figureCreationService;
     private final FigureUpdateService figureUpdateService;
 
     private CardLayout cardLayout;
 
     private Perspective activePerspective;
     private FigureEditPerspective editPerspective;
     private FigureExplorerPerspective explorerPerspective;
     private FigureMapperPerspective mapperPerspective;
     private FigureModel figureModel;
 
     private JMenuBar appMenuBar;
     private JMenu fileMenu;
     private JMenuItem fileMenuImportVideo;
     private JMenu perspectiveMenu;
     private JMenuItem perspectiveMenuFigureEditor;
     private JMenuItem perspectiveMenuFigureExplorer;
     private JMenuItem perspectiveMenuFigureMapper;
 
     private List<Image> iconImages;
 
     private File currentDir = null;
 
     public ApplicationFrame(Workspace ws) throws IOException {
         this.workspace = ws;
         this.figureStore = new FigureStore(ws, "/figures");
         this.treeStore = new FiguresTreeStore(ws, "/tree");
         this.beatPictureCache = new BeatPictureCache(ws, "/pics");
         this.videoMetaDataStore = new VideoMetaDataStore(ws, "/vids/meta");
         this.videoDir = new VideoDir(ws, "/vids", videoMetaDataStore);
         pregenerateMissingMetadata();
 
         this.figureCreationService = new FigureCreationService(ws, figureStore, videoDir, treeStore);
         this.figureUpdateService = new FigureUpdateService(ws, figureStore, treeStore, beatPictureCache);
 
         setTitle("Figurabia");
         Toolkit tk = Toolkit.getDefaultToolkit();
         iconImages = new ArrayList<Image>();
         iconImages.add(tk.getImage(getClass().getResource("res/figurabia_icon16__.png")));
         iconImages.add(tk.getImage(getClass().getResource("res/figurabia_icon32__.png")));
         iconImages.add(tk.getImage(getClass().getResource("res/figurabia_icon48.png")));
         setIconImages(iconImages);
         cardLayout = new CardLayout(0, 0);
         final Container contentPane = getContentPane();
         contentPane.setLayout(cardLayout); //new MigLayout("", "[fill]", "[fill]")
 
         final MediaPlayer player = new MediaPlayer();
         figureModel = new FigureModel();
         figureModel.addFigurePositionListener(new FigurePositionListener() {
             @Override
             public void update(Figure figure, int index) {
                 // set video of figure
                 String videoPath = "/vids/" + figure.getVideoName();
                 File videoFile = workspace.fileForReading(videoPath);
                 long initialPosition = 0;
                 if (index != -1) {
                     initialPosition = figure.getVideoPositions().get(index) / 1000000L;
                 }
                 VideoMetaData metaData = videoMetaDataStore.read(figure.getVideoName());
                 player.openVideo(videoFile, metaData.getMediaInfo(), initialPosition);
             }
         });
 
         // create and add FigureExplorerPerspective
         explorerPerspective = new FigureExplorerPerspective(workspace, figureStore, beatPictureCache, player,
                 figureModel);
         contentPane.add(explorerPerspective, explorerPerspective.getPerspectiveId());
 
         // create and add FigureEditPerspective
         editPerspective = new FigureEditPerspective(workspace, figureStore, treeStore, beatPictureCache,
                 figureCreationService,
                 figureUpdateService, player, figureModel);
         contentPane.add(editPerspective, editPerspective.getPerspectiveId());
 
         // create and add FigureMapperPerspective
         mapperPerspective = new FigureMapperPerspective(figureModel);
         contentPane.add(mapperPerspective, mapperPerspective.getPerspectiveId());
 
         // set up menu bar
         appMenuBar = new JMenuBar();
         setJMenuBar(appMenuBar);
 
         fileMenu = new JMenu("File");
         //fileMenu.getPopupMenu().setLightWeightPopupEnabled(false);
         appMenuBar.add(fileMenu);
         fileMenuImportVideo = new JMenuItem("Import Video...");
         fileMenuImportVideo.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent event) {
                 importVideo();
             }
         });
         fileMenu.add(fileMenuImportVideo);
 
         perspectiveMenu = new JMenu("Perspective");
         //perspectiveMenu.getPopupMenu().setLightWeightPopupEnabled(false);
         appMenuBar.add(perspectiveMenu);
 
         perspectiveMenuFigureEditor = new JRadioButtonMenuItem("Figure Editor");
         perspectiveMenuFigureEditor.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 switchToPerspective(editPerspective);
             }
         });
         perspectiveMenu.add(perspectiveMenuFigureEditor);
 
         perspectiveMenuFigureExplorer = new JRadioButtonMenuItem("Figure Explorer");
         perspectiveMenuFigureExplorer.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 switchToPerspective(explorerPerspective);
             }
         });
         perspectiveMenu.add(perspectiveMenuFigureExplorer);
 
         perspectiveMenuFigureMapper = new JRadioButtonMenuItem("Figure Mapper");
         perspectiveMenuFigureMapper.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 switchToPerspective(mapperPerspective);
             }
         });
         perspectiveMenu.add(perspectiveMenuFigureMapper);
 
         ButtonGroup perspectiveButtonGroup = new ButtonGroup();
         perspectiveButtonGroup.add(perspectiveMenuFigureEditor);
         perspectiveButtonGroup.add(perspectiveMenuFigureExplorer);
         perspectiveButtonGroup.add(perspectiveMenuFigureMapper);
         perspectiveMenuFigureExplorer.setSelected(true);
 
         // initialize active perspective
         switchToPerspective(explorerPerspective);
 
         // set frame properties
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
         setVisible(true);
     }
 
     private void importVideo() {
         // select video file
         JFileChooser fileChooser = new JFileChooser(currentDir);
         fileChooser.setMultiSelectionEnabled(false);
         if (fileChooser.showOpenDialog(ApplicationFrame.this) == JFileChooser.CANCEL_OPTION) {
             return;
         }
         currentDir = fileChooser.getCurrentDirectory();
 
         /*// open it with the video import view
         VideoImport videoImport = new VideoImport("file:"
                 + fileChooser.getSelectedFile().getAbsolutePath(),
                 ApplicationFrame.this.persistenceProvider, ApplicationFrame.this.workspace);
         SimplePanelFrame frame = new SimplePanelFrame(videoImport, 800, 600, false);
         frame.setIconImages(iconImages);
         frame.setTitle("Import Video " + fileChooser.getSelectedFile());*/
 
         // create a new figure
         Figure figure = null;
         try {
             figure = figureCreationService.createNewFigure(fileChooser.getSelectedFile());
         } catch (IOException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
         }
 
         switchToPerspective(editPerspective);
 
         // select the new figure
         if (figure != null) {
             figureModel.setCurrentFigure(figure, -1);
         }
     }
 
     private void pregenerateMissingMetadata() {
         List<String> videoPaths = workspace.list("/vids");
         for (String vp : videoPaths) {
            String metadataId = vp.replace("/vids", "/vids/meta");
            if (!videoMetaDataStore.exists(metadataId)) {
                String videoId = vp.substring("/vids/".length());
                 System.out.println("Pregenerating metadata for video " + videoId);
                 try {
                     videoDir.createAndStoreMetaData(videoId);
                 } catch (IOException e) {
                     System.err.println("Exception while creating meta data for " + videoId);
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private void switchToPerspective(Perspective perspective) {
         cardLayout.show(getContentPane(), perspective.getPerspectiveId());
         if (activePerspective != null) {
             activePerspective.updateOnPerspectiveSwitch(false);
         }
         activePerspective = perspective;
         activePerspective.updateOnPerspectiveSwitch(true);
     }
 
     public static void main(String[] args) {
         boolean black = args.length == 0 ? false : args[0].equals("black");
         Locale.setDefault(Locale.US);
         JFrame.setDefaultLookAndFeelDecorated(true);
         JDialog.setDefaultLookAndFeelDecorated(true);
         try {
             //UIManager.setLookAndFeel(new GTKLookAndFeel());
             if (black) {
                 UIManager.setLookAndFeel(new FigurabiaBlackLookAndFeel());
             } else {
                 UIManager.setLookAndFeel(new FigurabiaLookAndFeel());
             }
         } catch (UnsupportedLookAndFeelException e) {
             e.printStackTrace();
         }
 
         JNIMemoryManager.setMemoryModel(MemoryModel.NATIVE_BUFFERS);
 
         final Workspace w = new LocalFileWorkspace(new File("figurantdata"));
 
         // do UI construction in Swing's Event Dispatch Thread
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     new ApplicationFrame(w);
                 } catch (Exception e) {
                     e.printStackTrace();
                     JOptionPane.showMessageDialog(null, "Problem during startup:\n" + e.getLocalizedMessage());
                 }
             }
         });
     }
 }
