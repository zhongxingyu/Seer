 package cubetech.input;
 
 import cubetech.net.NetBuffer;
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  *
  * @author mads
  */
 public class PlayerInput {
     public Vector2f MousePos = new Vector2f(0.5f,0.5f);
     public Vector2f MouseDelta = new Vector2f();
     public int serverTime;
     public int WheelDelta;
     public boolean Mouse1;
     public boolean Mouse1Diff;
     public boolean Mouse2;
     public boolean Mouse2Diff;
     public boolean Mouse3;
     public boolean Mouse3Diff;
     public boolean Up;
     public boolean Down;
     public boolean Left;
     public boolean Right;
     public boolean Jump;
 
     public static PlayerInput ReadDeltaUserCmd(NetBuffer buf, PlayerInput oldcmd) {
         PlayerInput dest = new PlayerInput();
         dest.serverTime = buf.ReadInt();
         if(buf.ReadBool()) { // Got new data
             dest.Up = buf.ReadBool();
             dest.Down = buf.ReadBool();
             dest.Left = buf.ReadBool();
             dest.Right = buf.ReadBool();
             dest.Jump = buf.ReadBool();
             dest.Mouse1Diff = buf.ReadBool();
             if(dest.Mouse1Diff)
                 dest.Mouse1 = buf.ReadBool();
             dest.Mouse2Diff = buf.ReadBool();
             if(dest.Mouse2Diff)
                 dest.Mouse2 = buf.ReadBool();
             dest.Mouse3Diff = buf.ReadBool();
             if(dest.Mouse3Diff)
                 dest.Mouse3 = buf.ReadBool();
             boolean hasWheel = buf.ReadBool();
             if(hasWheel)
                 dest.WheelDelta = buf.ReadInt();
             dest.MousePos = buf.ReadVector();
         } else { // unchanged
             dest.Up = oldcmd.Up;
             dest.Down = oldcmd.Down;
             dest.Left = oldcmd.Left;
             dest.Right = oldcmd.Right;
             dest.Jump = oldcmd.Jump;
             dest.Mouse1Diff = oldcmd.Mouse1Diff;
             dest.Mouse2Diff = oldcmd.Mouse2Diff;
             dest.Mouse3Diff = oldcmd.Mouse3Diff;
             dest.Mouse1 = oldcmd.Mouse1;
             dest.Mouse2 = oldcmd.Mouse2;
             dest.Mouse3 = oldcmd.Mouse3;
             dest.WheelDelta = oldcmd.WheelDelta;
             dest.MousePos = oldcmd.MousePos;
         }
 
         return dest;
     }
 
     @Override
     public boolean equals(Object o) {
         if(!(o instanceof PlayerInput))
             return false;
 
 //        return false;
 
         return (o.hashCode() == this.hashCode());
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 37 * hash + (this.MousePos != null ? this.MousePos.hashCode() : 0);
         hash = 37 * hash + (this.MouseDelta != null ? this.MouseDelta.hashCode() : 0);
         hash = 37 * hash + this.serverTime;
         hash = 37 * hash + this.WheelDelta;
         hash = 37 * hash + (this.Mouse1 ? 1 : 0);
         hash = 37 * hash + (this.Mouse1Diff ? 1 : 0);
         hash = 37 * hash + (this.Mouse2 ? 1 : 0);
         hash = 37 * hash + (this.Mouse2Diff ? 1 : 0);
         hash = 37 * hash + (this.Mouse3 ? 1 : 0);
         hash = 37 * hash + (this.Mouse3Diff ? 1 : 0);
         hash = 37 * hash + (this.Up ? 1 : 0);
         hash = 37 * hash + (this.Down ? 1 : 0);
         hash = 37 * hash + (this.Left ? 1 : 0);
         hash = 37 * hash + (this.Right ? 1 : 0);
         hash = 37 * hash + (this.Jump ? 1 : 0);
         return hash;
     }
 
     public void WriteDeltaUserCmd(NetBuffer buf, PlayerInput from) {
         buf.Write(serverTime);
         if(this.equals(from)) {
             buf.Write(false); // no change
             return;
         }
 
         buf.Write(true);
         buf.Write(Up);
         buf.Write(Down);
         buf.Write(Left);
         buf.Write(Right);
         buf.Write(Jump);
         buf.Write(Mouse1Diff);
         if(Mouse1Diff)
             buf.Write(Mouse1);
         buf.Write(Mouse1Diff);
         if(Mouse1Diff)
             buf.Write(Mouse1);
         buf.Write(Mouse1Diff);
         if(Mouse1Diff)
             buf.Write(Mouse1);
         buf.Write(WheelDelta != 0);
         if(WheelDelta != 0)
             buf.Write(WheelDelta);
         buf.Write(MousePos);
     }
 
    PlayerInput Clone() {
         PlayerInput n = new PlayerInput();
         n.MousePos = new Vector2f();
         n.MousePos.x = MousePos.x;
         n.MousePos.y = MousePos.y;
         n.MouseDelta = new Vector2f();
         n.MouseDelta.x = MouseDelta.x;
         n.MouseDelta.y = MouseDelta.y;
         n.serverTime = serverTime;
         n.WheelDelta = WheelDelta;
         n.Mouse1 = Mouse1;
         n.Mouse1Diff = Mouse1Diff;
         n.Mouse2 = Mouse2;
         n.Mouse2Diff = Mouse2Diff;
         n.Mouse3 = Mouse3;
         n.Mouse3Diff = Mouse3Diff;
         n.Up = Up;
         n.Down = Down;
         n.Left = Left;
         n.Right = Right;
         n.Jump = Jump;
         return n;
     }
 }
