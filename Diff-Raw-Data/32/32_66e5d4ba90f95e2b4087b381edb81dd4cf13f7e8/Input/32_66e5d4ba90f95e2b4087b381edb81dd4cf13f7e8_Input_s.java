 package cubetech.input;
 
 import cubetech.common.Commands.ExecType;
 import cubetech.misc.Ref;
 import cubetech.net.ConnectState;
 import cubetech.ui.UI.MENU;
 import java.util.HashMap;
 import javax.swing.event.EventListenerList;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  * Handles input from frame to frame
  * @author mads
  */
 public class Input {
     public PlayerInput playerInput;
     Vector2f mouseDelta;
     
     Key[] keys;
     protected HashMap<Integer, EventListenerList> listenerList = new HashMap<Integer, EventListenerList>();
     protected HashMap<Integer, EventListenerList> listenerListMouse = new HashMap<Integer, EventListenerList>();
 
     private int KeyCatcher = 0;
 
     public int frame_msec = 0;
 
     public final static int KEYCATCH_NONE = 0;
     public final static int KEYCATCH_UI = 1;
     public final static int KEYCATCH_CONSOLE = 2;
     public final static int KEYCATCH_CGAME = 4;
     public final static int KEYCATCH_MESSAGE = 8;
 
     ButtonState in_left, in_right, in_forward, in_back;
     ButtonState[] in_buttons = new ButtonState[31]; // Custom buttons
 
     public Binds binds;
 
     // Custom mouse wheel code
     private int mWheelUpTime = 0;
     private int mWheelDownTime = 0;
 
     
 
     public Input() {
         binds = new Binds(this);
         InitButtons();
     }
 
     private void InitButtons() {
         in_forward = new ButtonState();
         in_back = new ButtonState();
         in_left = new ButtonState();
         in_right = new ButtonState();
         Ref.commands.AddCommand("+forward", in_forward.KeyDownHook);
         Ref.commands.AddCommand("-forward", in_forward.KeyUpHook);
         Ref.commands.AddCommand("+back", in_back.KeyDownHook);
         Ref.commands.AddCommand("-back", in_back.KeyUpHook);
         Ref.commands.AddCommand("+left", in_left.KeyDownHook);
         Ref.commands.AddCommand("-left", in_left.KeyUpHook);
         Ref.commands.AddCommand("+right", in_right.KeyDownHook);
         Ref.commands.AddCommand("-right", in_right.KeyUpHook);
 
         for (int i= 0; i < in_buttons.length; i++) {
             in_buttons[i] = new ButtonState();
             Ref.commands.AddCommand("+button" + i, in_buttons[i].KeyDownHook);
             Ref.commands.AddCommand("-button" + i, in_buttons[i].KeyUpHook);
         }
 
         binds.BindKey("W", "+forward");
         binds.BindKey("S", "+back");
         binds.BindKey("A", "+left");
         binds.BindKey("D", "+right");
         binds.BindKey("UP", "+forward");
         binds.BindKey("DOWN", "+back");
         binds.BindKey("LEFT", "+left");
         binds.BindKey("RIGHT", "+right");
         binds.BindKey("F10", "console");
         binds.BindKey("TAB", "+scores");
         binds.BindKey("RETURN", "message");
         binds.BindKey("y", "message");
     }
 
     public void SetKeyCatcher(int catcher) {
         if(catcher == KeyCatcher)
             return;
 
         ClearKeys();
         // Pause/unpause when entering/exiting menu
         if((KeyCatcher & KEYCATCH_UI) > 0
                 && (catcher & KEYCATCH_UI) == 0)
             Ref.cvars.Set2("cl_paused", "0", true);
         else if((KeyCatcher & KEYCATCH_UI) == 0
                 && (catcher & KEYCATCH_UI) > 0)
             Ref.cvars.Set2("cl_paused", "1", true);
         KeyCatcher = catcher;
     }
 
     public int GetKeyCatcher() {
         return KeyCatcher;
     }
 
     public Key GetKey(int index) {
         return keys[index];
     }
 
     void FireKeyEvent(KeyEvent evt) {
         Object[] listeners = null;
         if((KeyCatcher & KEYCATCH_CONSOLE) > 0 && listenerList.containsKey(KEYCATCH_CONSOLE)) {
             listeners = listenerList.get(KEYCATCH_CONSOLE).getListenerList();
         } else if((KeyCatcher & KEYCATCH_MESSAGE) > 0 && listenerList.containsKey(KEYCATCH_MESSAGE)) {
             listeners = listenerList.get(KEYCATCH_MESSAGE).getListenerList();
         } else if((KeyCatcher & KEYCATCH_UI) > 0 && listenerList.containsKey(KEYCATCH_UI)) {
             listeners = listenerList.get(KEYCATCH_UI).getListenerList();
         } else if((KeyCatcher & KEYCATCH_CGAME) > 0 && listenerList.containsKey(KEYCATCH_CGAME)) {
             listeners = listenerList.get(KEYCATCH_CGAME).getListenerList();
         }  else {
             Key key = (Key)evt.getSource();
             binds.ParseBinding(key.key, key.Pressed, key.Time);
             return;
         }
         
         for (int i= 0; i < listeners.length; i++) {
             if(listeners[i] == KeyEventListener.class) {
                 ((KeyEventListener)listeners[i+1]).KeyPressed(evt);
             }
         }
     }
 
     void FireMouseEvent(MouseEvent evt) {
         Object[] listeners = null;
        if((KeyCatcher & KEYCATCH_CONSOLE) > 0 && listenerListMouse.containsKey(KEYCATCH_CONSOLE)) {
            listeners = listenerListMouse.get(KEYCATCH_CONSOLE).getListenerList();
         } else if((KeyCatcher & KEYCATCH_MESSAGE) > 0 && listenerListMouse.containsKey(KEYCATCH_MESSAGE)) {
             listeners = listenerListMouse.get(KEYCATCH_MESSAGE).getListenerList();
         } else if((KeyCatcher & KEYCATCH_UI) > 0 && listenerListMouse.containsKey(KEYCATCH_UI)) {
             listeners = listenerListMouse.get(KEYCATCH_UI).getListenerList();
         } else if((KeyCatcher & KEYCATCH_CGAME) > 0 && listenerListMouse.containsKey(KEYCATCH_CGAME)) {
             listeners = listenerListMouse.get(KEYCATCH_CGAME).getListenerList();
        }  else {
//            if(evt.Button != -1) {
//                // Fire regular mouse button event
//                String button = "MOUSE" + (evt.Button + 1);
//                binds.ParseBinding(binds.StringToKey(button), evt.Pressed, Ref.client.realtime);
//            }
//
//            // event can also contain mousewheel info
//            // Wheeldelta don't have key-up events, so create it artificially
//            if(evt.WheelDelta < 0) {
//                binds.ParseBinding(binds.StringToKey("MWHEELDOWN"), true, Ref.client.realtime);
//                mWheelDownTime = Ref.client.realtime;
//            } else if(evt.WheelDelta > 0) {
//                binds.ParseBinding(binds.StringToKey("MWHEELUP"), true, Ref.client.realtime);
//                mWheelUpTime = Ref.client.realtime;
//            }
        }
 
         if(listeners == null)
             return;
 
         for (int i= 0; i < listeners.length; i++) {
             if(listeners[i] == MouseEventListener.class) {
                 ((MouseEventListener)listeners[i+1]).GotMouseEvent(evt);
             }
         }
     }
 
     public void Init() throws LWJGLException {
         // Keyboard init
         Keyboard.create();
         Keyboard.enableRepeatEvents(false);
         if(!Keyboard.isCreated())
             throw new LWJGLException("Keyboard not created.");
         keys = new Key[Binds.KEY_EXTENSION_END]; // Mouse keys extends the set
         for (int i= 0; i < keys.length; i++) {
             keys[i] = new Key(i);
         }
 
         // Mouse init
         Mouse.create();
 
         playerInput = new PlayerInput();
         mouseDelta = new Vector2f();
     }
     public void Update() {
         Display.processMessages();
         MouseUpdate();
         KeyboardUpdate();
         UpdateUserInput();
 
         // Send mwheel keyUp events if it's time
         if(mWheelDownTime != 0 && Ref.client.realtime - mWheelDownTime > 50) {
             binds.ParseBinding(binds.StringToKey("MWHEELDOWN"), false, (int) (Ref.client.realtime));
             mWheelDownTime = 0;
         }
         if(mWheelUpTime != 0 && Ref.client.realtime - mWheelUpTime > 50) {
             binds.ParseBinding(binds.StringToKey("MWHEELUP"), false, (int) (Ref.client.realtime));
             mWheelUpTime = 0;
         }
     }
 
     public void ClearKeys() {
         if(keys == null)
             return;
         for (int i= 0; i < keys.length; i++) {
             if(keys[i].Pressed)
                 keys[i].Changed = true;
             keys[i].Pressed = false;
         }
     }
 
     void UpdateUserInput() {
 //        if((GetKeyCatcher() & (KEYCATCH_CONSOLE | KEYCATCH_MESSAGE | KEYCATCH_UI)) > 0)
 //            return;
 
         
         playerInput.Up = ((int)Math.ceil(in_forward.KeyState()) == 1)?true:false;
         playerInput.Down = ((int)Math.ceil(in_back.KeyState()) == 1)?true:false;
         playerInput.Left = ((int)Math.ceil(in_left.KeyState()) == 1)?true:false;
         playerInput.Right = ((int)Math.ceil(in_right.KeyState()) == 1)?true:false;
     }
 
     public boolean IsKeyPressed(int key) {
         if(key <0 || key >= keys.length)
             return false;
         return keys[key].Pressed;
     }
 
     void MouseUpdate() {
         // Update mouse
         if(playerInput.Mouse1Diff)
             playerInput.Mouse1Diff = false;
         if(playerInput.Mouse2Diff)
             playerInput.Mouse2Diff = false;
         if(playerInput.Mouse3Diff)
             playerInput.Mouse3Diff = false;
         
         Vector2f delta = new Vector2f();
         boolean event = false;
         while(Mouse.next()) {
             event = true;
             // Add up delta
             delta.x += Mouse.getEventDX();
             delta.y += Mouse.getEventDY();
 
             // Set Position
             float mousex = (float)Mouse.getEventX() / (float)Ref.glRef.GetResolution().x;
             float mousey = (float)Mouse.getEventY() / (float)Ref.glRef.GetResolution().y;
             
             if(!Float.isInfinite(mousey) && !Float.isInfinite(mousex)) {
                 playerInput.MousePos.x = mousex;
                 playerInput.MousePos.y = mousey;
             }
 
             // Clamp to 0->1
             playerInput.MousePos.x = (float)Math.max(Math.min(playerInput.MousePos.x, 1f),0f);
             playerInput.MousePos.y = (float)Math.max(Math.min(playerInput.MousePos.y, 1f),0f);
 
             int wheelDelta = Mouse.getEventDWheel();
             playerInput.WheelDelta += wheelDelta;
             if(wheelDelta > 0)
                 wheelDelta = 1;
             if(wheelDelta < 0)
                 wheelDelta = -1;
 
             int button = Mouse.getEventButton();
             boolean pressed = false;
             if(button != -1)
                 pressed = Mouse.getEventButtonState();
             switch(button) {
                 case 0:
                     playerInput.Mouse1Diff =  playerInput.Mouse1 != pressed;
                     playerInput.Mouse1 = pressed;
                     break;
                 case 1:
                     playerInput.Mouse2Diff =  playerInput.Mouse2 != pressed;
                     playerInput.Mouse2 = pressed;
                     break;
                 case 2:
                     playerInput.Mouse3Diff =  playerInput.Mouse3 != pressed;
                     playerInput.Mouse3 = pressed;
                     break;
             }
             FireMouseEvent(new MouseEvent(button, pressed, wheelDelta, new Vector2f(delta.x, delta.y), new Vector2f(playerInput.MousePos.x,playerInput.MousePos.y)));
             // Also fire a key event for button presses
             if(button != -1) {
                 // Fire regular mouse button event
                 String buttonStr = "MOUSE" + (button + 1);
                 int keyIndex = binds.StringToKey(buttonStr);
                 Key key = keys[keyIndex];
                 key.Changed = true;
                 key.Pressed = pressed;
                 key.Time = Ref.client.realtime;
                 key.Name = buttonStr;
                 FireKeyEvent(new KeyEvent(key));
                //binds.ParseBinding(binds.StringToKey(buttonStr), pressed, Ref.client.realtime);
             }
 
             // event can also contain mousewheel info
             // Wheeldelta don't have key-up events, so create it artificially
             if(wheelDelta < 0) {
                 int keyIndex = binds.StringToKey("MWHEELDOWN");
                 Key key = keys[keyIndex];
                 key.Changed = true;
                 key.Pressed = true;
                 key.Time = Ref.client.realtime;
                 key.Name = "MWHEELDOWN";
                 FireKeyEvent(new KeyEvent(key));
//                binds.ParseBinding(binds.StringToKey("MWHEELDOWN"), true, Ref.client.realtime);
                 mWheelDownTime = Ref.client.realtime;
             } else if(wheelDelta > 0) {
                //String buttonStr = "MOUSE" + (button + 1);
                 int keyIndex = binds.StringToKey("MWHEELUP");
                 Key key = keys[keyIndex];
                 key.Changed = true;
                 key.Pressed = true;
                 key.Time = Ref.client.realtime;
                 key.Name = "MWHEELUP";
                 FireKeyEvent(new KeyEvent(key));
//                binds.ParseBinding(binds.StringToKey("MWHEELUP"), true, Ref.client.realtime);
                 mWheelUpTime = Ref.client.realtime;
             }
         }
         if(!event) {
             playerInput.WheelDelta = 0;
         }
 
         // Set delta
         playerInput.MouseDelta = delta;
     }
 
     void KeyboardUpdate() {
         
         for (int i= 0; i < keys.length; i++) {
             if(keys[i] != null)
                 keys[i].Changed = false;
         }
         
         int nProcessed = 0;
         while(Keyboard.next()) {
             boolean pressed = Keyboard.getEventKeyState();
             int key = Keyboard.getEventKey();
             char c = Keyboard.getEventCharacter();
             //int msec = (int) (Keyboard.getEventNanoseconds() / (1000 * 1000));
             int msec = Ref.client.realtime;
 
             Key currKey = keys[key];
             
             if(currKey.Pressed != pressed && currKey.Time <= msec) {
                 // Key changes state
                 currKey.Pressed = pressed;
                 currKey.Changed = true;
                 currKey.Time = msec;
                 currKey.Char = c;
                 if(pressed) {
                     // Special case handling for escape
                     // Toggle console
                     String keybind = binds.getBindForKey(key);
                     if((key == Keyboard.KEY_ESCAPE && IsKeyPressed(Keyboard.KEY_LSHIFT))
                             || (keybind != null && keybind.equalsIgnoreCase("console"))) {
                         Ref.commands.ExecuteText(ExecType.NOW, "console");
                         continue;
                     }
                     else if(key == Keyboard.KEY_ESCAPE) {
                         if((GetKeyCatcher() & KEYCATCH_CONSOLE) > 0) {
                             // Close console
                             Ref.Console.Close();
                             continue;
                         }
 
                         if((GetKeyCatcher() & KEYCATCH_MESSAGE) > 0) {
                             // TODO: Clear message
                             continue;
                         }
 
 //                        if((GetKeyCatcher() & KEYCATCH_CGAME) > 0) {
 ////                            SetKeyCatcher(GetKeyCatcher() & ~KEYCATCH_CGAME);
 //                            // TODO: Send null event to cgame?
 //                            continue;
 //                        }
 
                         if((GetKeyCatcher() & KEYCATCH_UI) == 0) {
                             if(Ref.client.state == ConnectState.ACTIVE)
                                 Ref.ui.SetActiveMenu(MENU.MAINMENU);
                             else if(Ref.client.state != ConnectState.DISCONNECTED) {
                                 // Escape can abort an connection attempt
                                 Ref.commands.ExecuteText(ExecType.NOW, "disconnect\n");
                                 Ref.ui.SetActiveMenu(MENU.MAINMENU);
                             }
                             continue;
                         } else {
                             SetKeyCatcher(GetKeyCatcher() & ~KEYCATCH_UI);
                         }
 
                         // TODO: Send keyevent to ui
                     }
                     // Alt+Enter toggles fullscreen
                     else if(key == Keyboard.KEY_RETURN && IsKeyPressed(Keyboard.KEY_LMENU)) {
                         int fs = Ref.cvars.Find("r_fullscreen").iValue + 1;
                         Ref.cvars.Set2("r_fullscreen", ""+ (fs&1), true);
                         continue;
                     }
                     
 
                     //System.out.println("Pressed: " + c);
                     
                 }
                 FireKeyEvent(new KeyEvent(currKey));
             }
 
             nProcessed++;
         }
         
     }
 
     
 
     public void AddKeyEventListener(KeyEventListener listener, int targetCatcher)
     {
         if(!listenerList.containsKey(targetCatcher))
             listenerList.put(targetCatcher, new EventListenerList());
 
         listenerList.get(targetCatcher).add(KeyEventListener.class, listener);
     }
 
     public void RemoveKeyEventListener(KeyEventListener listener, int target) {
         if(!listenerList.containsKey(target))
             return;
         listenerList.get(target).remove(KeyEventListener.class, listener);
     }
 
     public void AddMouseEventListener(MouseEventListener listener, int targetCatcher)
     {
         if(!listenerListMouse.containsKey(targetCatcher))
             listenerListMouse.put(targetCatcher, new EventListenerList());
 
         listenerListMouse.get(targetCatcher).add(MouseEventListener.class, listener);
 
     }
 
     public void RemoveMouseEventListener(MouseEventListener listener, int target) {
         if(!listenerListMouse.containsKey(target))
             return;
         listenerListMouse.get(target).remove(MouseEventListener.class, listener);
 
     }
 
     
 
     public PlayerInput CreateCmd() {
 
         return playerInput.Clone();
     }
 }
