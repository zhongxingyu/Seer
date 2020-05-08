 package deepcraft.core;
 
 public class Reference {	
	/* ID Reference Base */
	public static final int blockBaseID = 1740;
	public static final int itemBaseID = 7477;
	public static final int dimensionBaseID = 70;
 	//Blocks
	public static final int oreIDs = blockBaseID;
 	public static final int matBlockIDs = oreIDs + 32;
 	public static final int infurnaceIDs = matBlockIDs + 8;
 	public static final int artifactBlockIDs = infurnaceIDs + 2;
 	public static final int portalBlockIDs = artifactBlockIDs + 8;
 	public static final int miscBlockIDs = portalBlockIDs + 8;	
	//Items
	public static final int armorIDs = itemBaseID;
	public static final int toolIDs = armorIDs + 32;
	public static final int materialIDs = toolIDs + 64;
	public static final int artifactIDs = materialIDs + 24;
	public static final int miscItemIDs = artifactIDs + 8;
	//Dimensions	
 	public static final int enduraiID = dimensionBaseID+0;
 	public static final int xirkID = dimensionBaseID+1;
 	public static final int deepNetherID = dimensionBaseID+2;
 	public static final int valaID = dimensionBaseID+3;
 	public static final int sciID = dimensionBaseID+4;
 	public static final int godusID = dimensionBaseID+5;
 }
