 package packet.server;
 
public abstract class ServerPacket {
    public abstract String getPacketFormat();
 
    public abstract Object[] getParameters();
 
    public String render() {
        return this.toString();
    }

    public String toString() {
        return String.format(getPacketFormat(), getParameters());
    }
 }
