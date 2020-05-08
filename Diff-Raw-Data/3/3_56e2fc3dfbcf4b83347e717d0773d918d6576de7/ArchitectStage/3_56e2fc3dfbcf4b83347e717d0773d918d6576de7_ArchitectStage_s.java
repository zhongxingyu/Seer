 package eu32k.vJoy.architect;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 
 import eu32k.common.net.NetworkListener;
 import eu32k.common.net.Packet;
 import eu32k.vJoy.VJoyMain;
 import eu32k.vJoy.common.Colors;
 import eu32k.vJoy.common.workset.DataType;
 import eu32k.vJoy.common.workset.Instance;
 import eu32k.vJoy.common.workset.Port;
 import eu32k.vJoy.common.workset.Type;
 import eu32k.vJoy.common.workset.Workset;
 
 public class ArchitectStage extends Stage implements NetworkListener, Serializable {
    private static final long serialVersionUID = 4071133119506442487L;
 
    // private NetworkModule net;
    private ShapeRenderer rend;
 
    private Workset workset = Workset.getInstance();
 
    private HashMap<Instance, TypeView> map = new HashMap<Instance, TypeView>();
 
    private Instance selectedInstance = null;
    private Port selectedPort = null;
 
    private Object lock = new Object();
 
    public ArchitectStage(/* NetworkModule net */) {
       // this.net = net;
       rend = new ShapeRenderer();
 
       Table table = new Table();
       table.setFillParent(true);
       table.top();
 
       final SelectBox booleanSelectionBox = new SelectBox(workset.booleanTypes.toArray(), VJoyMain.SKIN);
       booleanSelectionBox.setColor(Colors.DARK_YELLOW);
 
       TextButton booleanAddButtion = new TextButton("Add", VJoyMain.SKIN);
       booleanAddButtion.setColor(DataType.BOOLEAN.getNormalColor());
 
       booleanAddButtion.addListener(new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             Instance instance = workset.booleanTypes.get(booleanSelectionBox.getSelectionIndex()).instanciate(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
             workset.getInstances().add(instance);
             setSelectedInstance(instance);
             return false;
          }
       });
 
       final SelectBox numberSelectionBox = new SelectBox(workset.numberTypes.toArray(), VJoyMain.SKIN);
       numberSelectionBox.setColor(Colors.DARK_YELLOW);
 
       TextButton numberAddButtion = new TextButton("Add", VJoyMain.SKIN);
       numberAddButtion.setColor(DataType.NUMBER.getNormalColor());
 
       numberAddButtion.addListener(new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             Instance instance = workset.numberTypes.get(numberSelectionBox.getSelectionIndex()).instanciate(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
             workset.getInstances().add(instance);
             setSelectedInstance(instance);
             return false;
          }
       });
 
       final SelectBox imageSelectionBox = new SelectBox(workset.imageTypes.toArray(), VJoyMain.SKIN);
       imageSelectionBox.setColor(Colors.DARK_YELLOW);
 
       TextButton imageAddButtion = new TextButton("Add", VJoyMain.SKIN);
       imageAddButtion.setColor(DataType.IMAGE.getNormalColor());
 
       imageAddButtion.addListener(new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             Instance instance = workset.imageTypes.get(imageSelectionBox.getSelectionIndex()).instanciate(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
             workset.getInstances().add(instance);
             setSelectedInstance(instance);
             return false;
          }
       });
 
       TextButton deleteButton = new TextButton("Delete", VJoyMain.SKIN);
       deleteButton.setColor(Colors.DARK_YELLOW);
       deleteButton.addListener(new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             if (selectedInstance != null) {
                for (Instance instance : workset.getInstances()) {
                   ArrayList<Port> toRemove = new ArrayList<Port>();
                   for (Port port : instance.getPortMapping().keySet()) {
                      if (instance.getPortMapping().get(port) == selectedInstance) {
                         toRemove.add(port);
                      }
                   }
                   for (Port port : toRemove) {
                      instance.getPortMapping().remove(port);
                   }
                }
                workset.getInstances().remove(selectedInstance);
                selectedInstance = null;
             }
             return false;
          }
       });
 
       TextButton setExitButton = new TextButton("Set as exit", VJoyMain.SKIN);
       setExitButton.setColor(Colors.DARK_YELLOW);
       setExitButton.addListener(new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             if (selectedInstance != null && selectedInstance.getType().getDataType() == DataType.IMAGE) {
                workset.setExitInstance(selectedInstance);
             }
             return false;
          }
       });
 
       TextButton screenButton = new TextButton("Screen", VJoyMain.SKIN);
       screenButton.setColor(Colors.DARK_YELLOW);
       screenButton.addListener(new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             // MainMenuStage.vJoy.changeStage(VJoyMain.screenStage);
             return false;
          }
       });
 
       table.add(booleanSelectionBox).fill().expandX().pad(3);
       table.add(booleanAddButtion).fill().pad(3);
       table.row();
       table.add(numberSelectionBox).fill().expandX().pad(3);
       table.add(numberAddButtion).fill().pad(3);
       table.row();
       table.add(imageSelectionBox).fill().expandX().pad(3);
       table.add(imageAddButtion).fill().pad(3);
       table.row();
       table.add();
       table.add(deleteButton).fill().pad(3);
       table.row();
       table.add();
       table.add(setExitButton).fill().pad(3);
 
       addActor(table);
 
       // net.addNetworkListener(this);
       // net.start();
    }
 
    @Override
    public void draw() {
       synchronized (lock) {
          // remove old actors ---
          ArrayList<Instance> toRemove = new ArrayList<Instance>();
          for (Instance instance : map.keySet()) {
             if (!workset.getInstances().contains(instance)) {
                toRemove.add(instance);
             }
          }
 
          for (Instance instance : toRemove) {
             getRoot().removeActor(map.get(instance));
             map.remove(instance);
          }
          // ---------------------
 
          rend.begin(ShapeType.Line);
 
          // float pos = 0;
          // for (Port port : currentPatch.getPorts()) {
          // PortView view = map2.get(port);
          // if (view == null) {
          // view = new PortView(port);
          // map2.put(port, view);
          // addActor(view);
          // }
          // view.setPosition(0, pos);
          // view.update();
          // pos += view.getHeight() * 1.2f;
          // }
 
          for (Instance instance : workset.getInstances()) {
             Type subType = instance.getType();
 
             TypeView view1 = map.get(instance);
             if (view1 == null) {
                view1 = new TypeView(this, subType, instance);
                map.put(instance, view1);
                addActor(view1);
             }
 
             if (view1.isDragging()) {
                instance.setX(view1.getX());
                instance.setY(view1.getY());
             } else {
                view1.setX(instance.getX());
                view1.setY(instance.getY());
             }
 
             view1.update();
 
             for (Port port : subType.getPorts()) {
                rend.setColor(port.getDataType().getNormalColor());
                Instance linkTo = instance.getPortMapping().get(port);
                if (linkTo != null) {
                   TypeView view2 = map.get(linkTo);
                   if (view2 != null) {
                      rend.line(view1.getPortX(port), view1.getPortY(port), view2.getConnectorX(), view2.getConnectorY());
                   }
                }
 
                if (instance == selectedInstance && port == selectedPort) {
                   rend.line(view1.getPortX(port), view1.getPortY(port), Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
                }
             }
             if (instance == workset.getExitInstance()) {
                rend.setColor(subType.getDataType().getNormalColor());
                rend.line(view1.getConnectorX(), view1.getConnectorY(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2);
             }
 
          }
          rend.end();
          super.draw();
       }
    }
 
    @Override
    public void packetReceived(Packet packet) {
       // if (packet.getPayload() == null) {
       // return;
       // }
       //
       // Object payload = null;
       // try {
       // payload = Serializer.deserialize(packet.getPayload());
       // } catch (Exception e1) {
       // // NOP
       // }
       //
       // if (payload == null) {
       // return;
       // }
       //
       // if (packet.getName().equals(net.getPacket().getName()) && payload
       // instanceof Workset) {
       // synchronized (lock) {
       // workset = (Workset) payload;
       // currentPatch = workset.mainPatch;
       // }
       // }
    }
 
    public Instance getSelectedInstance() {
       return selectedInstance;
    }
 
    public void setSelectedInstance(Instance selectedInstance) {
       if (this.selectedInstance != selectedInstance) {
          if (selectedPort != null) {
             if (selectedInstance.getType().getDataType() == selectedPort.getDataType()) {
                this.selectedInstance.getPortMapping().put(selectedPort, selectedInstance);
                selectedPort = null;
             }
             return;
          }
       }
       this.selectedInstance = selectedInstance;
    }
 
    public Port getSelectedPort() {
       return selectedPort;
    }
 
    public void setSelectedPort(Instance selectedInstance, Port selectedPort) {
       if (this.selectedInstance == selectedInstance) {
          if (this.selectedPort == selectedPort) {
             this.selectedPort = null;
          } else {
             this.selectedPort = selectedPort;
             selectedInstance.getPortMapping().remove(selectedPort);
          }
       }
    }
 }
