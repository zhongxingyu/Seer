 package teamcerberus.cerberustech.energy.generators;
 
 import net.minecraft.tileentity.TileEntity;
 
 public enum GeneratorType {
 	Coal("cerberusGeneratorCoal", "Coal Generator", TileEntityCoalGenerator.class, 100, 1000),
 	Solar("cerberusGeneratorSolar", "Solar Generator", TileEntityGeneratorSolar.class, 10, 1000);
 	
 	private String unlocalizedName;
 	private String localizedName;
 	private int output;
 	private Class<? extends TileEntityGeneratorBase> claSS;
 	private int internalBuffer;
 	
 	private GeneratorType(String unlocalizaedName, String localizedName, Class<? extends TileEntityGeneratorBase> claSS, int output, int internalBuffer) {
 		this.unlocalizedName = unlocalizaedName;
 		this.localizedName = localizedName;
 		this.output = output;
 		this.internalBuffer = internalBuffer;
 	}
 	
 	public TileEntity createTileEntity() {
 		try {
 			return claSS.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public String getUnlocalizedName() {
 		return unlocalizedName;
 	}
 
 	public String getLocalizedName() {
 		return localizedName;
 	}
 
 	public int getOutput() {
 		return output;
 	}
 
 	public int getInternalBuffer() {
 		return internalBuffer;
 	}
 
 }
