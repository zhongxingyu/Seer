 package en.trudan.ConstructionSites.Data;
 
 import java.io.Serializable;
 
 import org.bukkit.Material;
 
 public class Blockdata implements Serializable{
 
 	private static final long serialVersionUID = -7765686518366269441L;
 	private int blockvalue = 0;
	private byte data = 0x0000;
 
 	public Blockdata(){
 
 	}
 	public Blockdata(int blockvalue){
 		this.blockvalue = blockvalue;	
 	}
 	public Blockdata(int blockvalue, byte data){
 		this.blockvalue = blockvalue;
 		this.data = data;
 	}
 	public Blockdata(Material material){
 		this.blockvalue = material.getId();	
 	}
 	public Blockdata(Material material, byte data){
 		this.blockvalue = material.getId();
 		this.data = data;
 	}
 
 
 	public void setBlockvalue(int blockvalue) {
 		this.blockvalue = blockvalue;
 	}
 	public int getBlockvalue() {
 		return blockvalue;
 	}
 	public void setData(byte data) {
 		this.data = data;
 	}
 	public byte getData() {
 		return data;
 	}
 
 }
