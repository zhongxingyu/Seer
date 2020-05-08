 package com.deadmoose.flumptools.flumpview;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import com.google.common.collect.Lists;
 
 import com.deadmoose.flumptools.playn.FileAssets;
 
 import react.Functions;
 import react.Slot;
 import react.UnitSlot;
 import react.Value;
 import react.ValueView;
 
 import pythagoras.f.Point;
 
 import playn.core.GroupLayer;
 import playn.core.ImmediateLayer;
 import playn.core.Key;
 import playn.core.Keyboard;
 import playn.core.Layer;
 import playn.core.Mouse;
 import playn.core.PlayN;
 import playn.core.Pointer;
 import playn.core.Surface;
 import playn.core.util.Callback;
 import playn.core.util.Clock;
 import playn.java.JavaPlatform;
 
 import tripleplay.flump.JsonLoader;
 import tripleplay.flump.Library;
 import tripleplay.flump.Movie;
 import tripleplay.flump.MoviePlayer;
 import tripleplay.flump.Symbol;
 import tripleplay.flump.Texture;
 import tripleplay.game.ScreenStack;
 import tripleplay.game.UIScreen;
 import tripleplay.ui.Background;
 import tripleplay.ui.Button;
 import tripleplay.ui.CheckBox;
 import tripleplay.ui.Group;
 import tripleplay.ui.Label;
 import tripleplay.ui.Root;
 import tripleplay.ui.Scroller;
 import tripleplay.ui.Shim;
 import tripleplay.ui.SimpleStyles;
 import tripleplay.ui.Slider;
 import tripleplay.ui.Style;
 import tripleplay.ui.Tabs;
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
 
         final GroupLayer baseLayer = PlayN.graphics().createGroupLayer();
         Shim shim = new Shim(1, 1) {
             @Override protected void layout () {
                 super.layout();
 
                 baseLayer.setTranslation(size().width()/2, size().height()/2);
             }
         };
         shim.layer.add(baseLayer);
         baseLayer.add(_pannedLayer);
         _root.add(shim.setConstraint(BorderLayout.CENTER));
 
         _pannedLayer.add(_flumpLayer);
         _pannedLayer.add(PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
             public void render (Surface surface) {
                 // Crosshair at the origin
                 surface.setFillColor(Colors.WHITE);
                 surface.fillRect(-10, -1, 20, 2);
                 surface.fillRect(-1, -10, 2, 20);
             }
         }).setDepth(1));
 
         _pannedLayer.setHitTester(new Layer.HitTester() {
             @Override public Layer hitTest (Layer layer, Point p) {
                 return layer;
             }
         });
         _pannedLayer.addListener(new Pointer.Adapter() {
             @Override public void onPointerStart (Pointer.Event event) {
                 _lastX = event.x();
                 _lastY = event.y();
             }
 
             @Override public void onPointerDrag (Pointer.Event event) {
                 float dx = _lastX - event.x();
                 float dy = _lastY - event.y();
 
                 _pannedLayer.setTranslation(_pannedLayer.tx() - dx, _pannedLayer.ty() - dy);
 
                 _lastX = event.x();
                 _lastY = event.y();
             }
 
             protected float _lastX, _lastY;
         });
         _pannedLayer.addListener(new Mouse.LayerAdapter() {
             @Override public void onMouseWheelScroll (Mouse.WheelEvent event) {
                 if (event.velocity() < 0) {
                     zoomIn(-event.velocity());
                 } else {
                     zoomOut(event.velocity());
                 }
             }
         });
         PlayN.keyboard().setListener(new Keyboard.Adapter() {
             @Override public void onKeyDown (Keyboard.Event event) {
                 if (event.key() == Key.HOME) {
                     _pannedLayer.setTranslation(0, 0);
                 }
             }
             @Override public void onKeyTyped (Keyboard.TypedEvent event) {
                 switch (event.typedChar()) {
                 case '-':
                     zoomOut(1);
                     break;
                 case '+':
                     zoomIn(1);
                     break;
                 case '=':
                 case '1':
                     _zoom.update(1f);
                     break;
                 }
             }
         });
 
         _zoom.connectNotify(new ValueView.Listener<Float>() {
             @Override public void onChange (Float value, Float oldValue) {
                 _flumpLayer.setScale(value);
             }
         });
         _flumpLayer.add(_textureLayer);
 
         Button loadButton = new Button("Load Library");
         loadButton.clicked().connect(new UnitSlot() {
             @Override public void onEmit () {
                 openFileChooser();
             }
         });
         _status = new Label().addStyles(Style.COLOR.is(Colors.RED));
         _status.text.connectNotify(new Slot<String>() {
             @Override public void onEmit (String str) {
                 _status.setVisible(str != null && !str.isEmpty());
             }
         });
         Group bottomGroup = new Group(AxisLayout.vertical());
         bottomGroup.add(loadButton);
         bottomGroup.add(_status);
         _root.add(bottomGroup.setConstraint(BorderLayout.NORTH));
 
         _movies = new Group(AxisLayout.vertical().stretchByDefault().offStretch());
         _textures = new Group(AxisLayout.vertical().stretchByDefault().offStretch());
 
         Tabs tabs = new Tabs();
         tabs.add("Movies", new Scroller(_movies));
         tabs.add("Textures", new Scroller(_textures));
 
         _root.add(tabs.setConstraint(BorderLayout.EAST));
 
         _autoplay = new CheckBox();
         _autoplay.checked.update(true);
 
         _slider = new Slider(0, 0, 1);
         _slider.addStyles(Style.BACKGROUND.is(Background.solid(Colors.WHITE)));
         _slider.value.connect(new Slot<Float>() {
             @Override public void onEmit (Float value) {
                 if (_autoplay.checked.get()) {
                     // We aren't driving things
                     return;
                 }
                 _player.movie().setPosition(value * _player.movie().symbol().duration);
             }
         });
 
         Group scrubber = new Group(AxisLayout.horizontal());
         scrubber.add(_autoplay);
         scrubber.add(_slider.setConstraint(AxisLayout.stretched()));
 
         _autoplay.checked.connectNotify(_slider.enabledSlot().compose(Functions.NOT));
 
         _root.add(scrubber.setConstraint(BorderLayout.SOUTH));
 
         openFileChooser();
     }
 
     @Override public void wasHidden ()
     {
         super.wasHidden();
 
         iface.destroyRoot(_root);
     }
 
     @Override public void paint (Clock clock)
     {
         super.paint(clock);
         if (_player != null && _autoplay.checked.get()) {
             _player.paint(clock);
             _slider.value.update(_player.movie().position() / _player.movie().symbol().duration);
         }
     }
 
     protected void openFileChooser ()
     {
        new Thread(new Runnable() {
            public void run () {
                if (_chooser != null) {
                    _chooser.cancelSelection();
                }
 
                _chooser = new JFileChooser(PlayN.storage().getItem(PREF_KEY));
                _chooser.setFileFilter(new FileNameExtensionFilter("Flump libraries", "json"));
 
                int returnVal = _chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadFlumpLibrary(_chooser.getSelectedFile());
                    _chooser = null;
                }
            }
        }).start();
     }
 
     protected void loadFlumpLibrary (File file)
     {
         JavaPlatform platform = (JavaPlatform)PlayN.platform();
         platform.setTitle("Flumpview - " + file.getParentFile().getName());
         FileAssets assets = new FileAssets(platform);
         _status.text.update("");
         PlayN.storage().setItem(PREF_KEY, file.getParentFile().getParentFile().getAbsolutePath());
         JsonLoader.loadLibrary(assets, file.getParentFile().getAbsolutePath(), new Callback<Library>() {
             public void onSuccess (final Library library) {
                 if (_player != null) {
                     _player.destroy();
                 }
 
                 _player = new MoviePlayer(library);
                 _flumpLayer.add(_player.layer());
 
                 _movies.destroyAll();
                 _textures.destroyAll();
 
                 List<String> movies = Lists.newArrayList();
                 List<String> textures = Lists.newArrayList();
 
                 for (Map.Entry<String, Symbol> entry : library.symbols.entrySet()) {
                     if (!entry.getKey().startsWith("~")) {
                         if (entry.getValue() instanceof Movie.Symbol) {
                             movies.add(entry.getKey());
                         } else if (entry.getValue() instanceof Texture.Symbol) {
                             textures.add(entry.getKey());
                         }
                     }
                 }
 
                 Collections.sort(movies);
                 Collections.sort(textures);
 
                 final List<ToggleButton> buttons = Lists.newArrayList();
 
                 for (final String movie : movies) {
                     final ToggleButton button = new ToggleButton(movie);
                     buttons.add(button);
                     _movies.add(button);
                     button.selected().connect(new ValueView.Listener<Boolean>() {
                         @Override public void onChange (Boolean value, Boolean oldValue) {
                             if (value) {
                                 _player.layer().setVisible(true);
                                 _textureLayer.setVisible(false);
 
                                 _player.loop(movie);
                             }
                         }
                     });
                 }
                 for (final String texture : textures) {
                     final ToggleButton button = new ToggleButton(texture);
                     buttons.add(button);
                     _textures.add(button);
 
                     button.selected().connect(new ValueView.Listener<Boolean>() {
                         @Override public void onChange (Boolean value, Boolean oldValue) {
                             if (value) {
                                 _player.layer().setVisible(false);
                                 _textureLayer.setVisible(true);
 
                                 _textureLayer.removeAll();
                                 _textureLayer.add(library.createTexture(texture).layer());
                             }
                         }
                     });
                 }
 
                 for (final ToggleButton button : buttons) {
                     button.selected().connect(new ValueView.Listener<Boolean>() {
                         @Override public void onChange (Boolean value, Boolean oldValue) {
                             if (value) {
                                 for (ToggleButton otherButton : buttons) {
                                     if (otherButton != button) {
                                         otherButton.selected().update(false);
                                     }
                                 }
                             }
                         }
                     });
                 }
 
                 ((ToggleButton)_movies.childAt(0)).selected().update(true);
             }
 
             public void onFailure (Throwable cause) {
                 PlayN.log().error("Error loading flump", cause);
                 _status.text.update("Error loading flump: " + cause.getMessage());
             }
         });
     }
 
     protected void zoomIn (float clicks)
     {
         _zoom.update(_zoom.get() * clicks * 1.1f);
     }
 
     protected void zoomOut (float clicks)
     {
         _zoom.update(_zoom.get() * clicks * 0.9f);
     }
 
     protected Root _root;
     protected ScreenStack _screens;
     protected JFileChooser _chooser;
     protected MoviePlayer _player;
     protected GroupLayer _pannedLayer = PlayN.graphics().createGroupLayer();
     protected GroupLayer _flumpLayer = PlayN.graphics().createGroupLayer();
     protected GroupLayer _textureLayer = PlayN.graphics().createGroupLayer();
     protected Group _movies;
     protected Group _textures;
     protected Label _status;
 
     protected Value<Float> _zoom = Value.create(1f);
 
     protected CheckBox _autoplay;
     protected Slider _slider;
 
     protected static final String PREF_KEY = "FlumpViewPath";
 }
