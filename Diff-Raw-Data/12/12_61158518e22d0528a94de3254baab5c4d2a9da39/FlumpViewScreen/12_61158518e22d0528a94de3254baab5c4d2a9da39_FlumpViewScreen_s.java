 package com.deadmoose.flumptools.flumpview;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import com.google.common.collect.Lists;
 
 import playn.core.GroupLayer;
 import playn.core.ImmediateLayer;
 import playn.core.PlayN;
 import playn.core.Surface;
 import playn.core.util.Callback;
 import playn.core.util.Clock;
 import playn.java.JavaPlatform;
 
 import react.UnitSlot;
 import react.Value;
 import react.ValueView;
 
 import tripleplay.flump.JsonLoader;
 import tripleplay.flump.Library;
 import tripleplay.flump.Movie;
 import tripleplay.flump.MoviePlayer;
 import tripleplay.flump.Symbol;
 import tripleplay.game.ScreenStack;
 import tripleplay.game.UIScreen;
 import tripleplay.ui.Button;
 import tripleplay.ui.Group;
 import tripleplay.ui.Root;
 import tripleplay.ui.Scroller;
 import tripleplay.ui.Shim;
 import tripleplay.ui.SimpleStyles;
 import tripleplay.ui.ToggleButton;
 import tripleplay.ui.layout.AxisLayout;
 import tripleplay.ui.layout.BorderLayout;
 import tripleplay.util.Colors;
 
 public class FlumpViewScreen extends UIScreen
 {
     public final Value<Library> library = Value.create(null);
 
     public FlumpViewScreen (ScreenStack screens)
     {
         _screens = screens;
     }
 
     @Override public void wasShown ()
     {
         super.wasShown();
 
         _root = iface.createRoot(new BorderLayout(), SimpleStyles.newSheet(), layer);
         _root.setSize(width(), height());
 
         Shim shim = new Shim(1, 1) {
             @Override protected void layout () {
                 super.layout();
 
                 _flumpLayer.setTranslation(size().width()/2, size().height()/2);
             }
         };
         shim.layer.add(_flumpLayer);
         _root.add(shim.setConstraint(BorderLayout.CENTER));
 
         _flumpLayer.add(PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
             public void render (Surface surface) {
                 surface.setFillColor(Colors.WHITE);
                 surface.fillRect(-10, -1, 20, 2);
                 surface.fillRect(-1, -10, 2, 20);
             }
         }).setDepth(1));
 
         Button loadButton = new Button("Load Library");
         loadButton.clicked().connect(new UnitSlot() {
             @Override public void onEmit () {
                 openFileChooser();
             }
         });
         _root.add(loadButton.setConstraint(BorderLayout.SOUTH));
 
         _movies = new Group(AxisLayout.vertical());
 
         _root.add(new Scroller(_movies).setConstraint(BorderLayout.EAST));
 
         openFileChooser();
     }
 
     @Override public void wasHidden ()
     {
         super.wasHidden();
 
         iface.destroyRoot(_root);
     }
 
     @Override public void paint (Clock clock) {
         super.paint(clock);
         if (_player != null) {
             _player.paint(clock);
         }
     }
 
     protected void openFileChooser ()
     {
         if (_chooser != null) {
             _chooser.cancelSelection();
         }
 
         _chooser = new JFileChooser(PlayN.storage().getItem(PREF_KEY));
         _chooser.setFileFilter(new FileNameExtensionFilter("Flump libraries", "json"));
 
         int returnVal = _chooser.showOpenDialog(null);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             loadFlumpLibrary(_chooser.getSelectedFile());
         }
     }
 
     protected void loadFlumpLibrary (File file)
     {
         ((JavaPlatform)PlayN.platform()).setTitle("Flumpview - " + file.getParentFile().getName());
         PlayN.storage().setItem(PREF_KEY, file.getParentFile().getParentFile().getAbsolutePath());
         JsonLoader.loadLibrary(file.getParentFile().getAbsolutePath(), new Callback<Library>() {
             public void onSuccess (Library library) {
                 if (_player != null) {
                     _player.destroy();
                 }
 
                 _player = new MoviePlayer(library);
                 _flumpLayer.add(_player.layer());
 
                 _movies.destroyAll();
 
                 List<String> movies = Lists.newArrayList();
 
                 for (Map.Entry<String, Symbol> entry : library.symbols.entrySet()) {
                     if (!entry.getKey().startsWith("~") &&
                         entry.getValue() instanceof Movie.Symbol) {
                         movies.add(entry.getKey());
                     }
                 }
 
                 Collections.sort(movies);
 
                 final List<ToggleButton> buttons = Lists.newArrayList();
 
                 for (final String movie : movies) {
                     final ToggleButton button = new ToggleButton(movie);
                     buttons.add(button);
                     button.selected().connect(new ValueView.Listener<Boolean>() {
                         @Override public void onChange (Boolean value, Boolean oldValue) {
                             if (value) {
                                 _player.loop(movie);
 
                                 for (ToggleButton otherButton : buttons) {
                                     if (otherButton != button) {
                                         otherButton.selected().update(false);
                                     }
                                 }
                             }
                         }
                     });
 
                     _movies.add(button);
                 }
 
                 ((ToggleButton)_movies.childAt(0)).selected().update(true);
             }
 
             public void onFailure (Throwable cause) {
                 PlayN.log().error("Error loading flump", cause);
             }
         });
     }
 
     protected Root _root;
     protected ScreenStack _screens;
     protected JFileChooser _chooser;
     protected MoviePlayer _player;
     protected GroupLayer _flumpLayer = PlayN.graphics().createGroupLayer();
     protected Group _movies;
 
     protected static final String PREF_KEY = "FlumpViewPath";
 }
