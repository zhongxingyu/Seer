 package retsrif.Slopes;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.block.design.SubTexture;
 import org.getspout.spoutapi.block.design.Texture;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
 import org.getspout.spoutapi.material.CustomBlock;
 import org.getspout.spoutapi.material.MaterialData;
 
 public class Slopes extends JavaPlugin {
 	public static Texture tex;
 	public static SubTexture wood;
 	public static SubTexture cobble;
 	public static SubTexture glass;
 	public static SubTexture sandstone;
 	public static SubTexture dirt;
 	public static SubTexture stone;
 	public static SubTexture sand;
 	public static SubTexture snow;
 	public static SubTexture grass;
 	public static SubTexture bricks;
 	public static SubTexture gravel;
 	public static SubTexture gold;
 	public static SubTexture iron;
 	public static SubTexture obsidian;
 	public static SubTexture diamond;
 	public static SubTexture netherbrick;
 
 	public static CustomBlock gls;
 	public static CustomBlock sss;
 	public static CustomBlock dirs;
 	public static CustomBlock sas;
 	public static CustomBlock sns;
 	public static CustomBlock grss;
 	public static CustomBlock gravs;
 	public static CustomBlock gos;
 	public static CustomBlock is;
 	public static CustomBlock os;
 	public static CustomBlock dias;
 	
 	public static CustomBlock wcs;
 	public static CustomBlock ccs;
 	public static CustomBlock glcs;
 	public static CustomBlock sscs;
 	public static CustomBlock dircs;
 	public static CustomBlock stcs;
 	public static CustomBlock sacs;
 	public static CustomBlock sncs;
 	public static CustomBlock grascs;
 	public static CustomBlock bcs;
 	public static CustomBlock gravcs;
 	public static CustomBlock gocs;
 	public static CustomBlock ics;
 	public static CustomBlock ocs;
 	public static CustomBlock diacs;
 	public static CustomBlock ncs;
 	
 	public static CustomBlock wsf;
 	public static CustomBlock csf;
 	public static CustomBlock glsf;
 	public static CustomBlock sssf;
 	public static CustomBlock dirsf;
 	public static CustomBlock stsf;
 	public static CustomBlock sasf;
 	public static CustomBlock snsf;
 	public static CustomBlock grassf;
 	public static CustomBlock bsf;
 	public static CustomBlock gravsf;
 	public static CustomBlock gosf;
 	public static CustomBlock isf;
 	public static CustomBlock osf;
 	public static CustomBlock diasf;
 	public static CustomBlock nsf;
 	
 	public static CustomBlock wsc;
 	public static CustomBlock csc;
 	public static CustomBlock glsc;
 	public static CustomBlock sssc;
 	public static CustomBlock dirsc;
 	public static CustomBlock stsc;
 	public static CustomBlock sasc;
 	public static CustomBlock snsc;
 	public static CustomBlock grassc;
 	public static CustomBlock bsc;
 	public static CustomBlock gravsc;
 	public static CustomBlock gosc;
 	public static CustomBlock isc;
 	public static CustomBlock osc;
 	public static CustomBlock diasc;
 	public static CustomBlock nsc;
 	
 	public static CustomBlock wc;
 	public static CustomBlock cc;
 	public static CustomBlock glc;
 	public static CustomBlock ssc;
 	public static CustomBlock dirc;
 	public static CustomBlock stc;
 	public static CustomBlock sac;
 	public static CustomBlock snc;
 	public static CustomBlock grasc;
 	public static CustomBlock bc;
 	public static CustomBlock gravc;
 	public static CustomBlock goc;
 	public static CustomBlock ic;
 	public static CustomBlock oc;
 	public static CustomBlock diac;
 	public static CustomBlock nc;
 	
 	public static CustomBlock wsa;
 	public static CustomBlock csa;
 	public static CustomBlock glsa;
 	public static CustomBlock sssa;
 	public static CustomBlock dirsa;
 	public static CustomBlock stsa;
 	public static CustomBlock sasa;
 	public static CustomBlock snsa;
 	public static CustomBlock grassa;
 	public static CustomBlock bsa;
 	public static CustomBlock gravsa;
 	public static CustomBlock gosa;
 	public static CustomBlock isa;
 	public static CustomBlock osa;
 	public static CustomBlock diasa;
 	public static CustomBlock nsa;
 	
 	public static CustomBlock wca;
 	public static CustomBlock cca;
 	public static CustomBlock glca;
 	public static CustomBlock ssca;
 	public static CustomBlock dirca;
 	public static CustomBlock stca;
 	public static CustomBlock saca;
 	public static CustomBlock snca;
 	public static CustomBlock grasca;
 	public static CustomBlock bca;
 	public static CustomBlock gravca;
 	public static CustomBlock goca;
 	public static CustomBlock ica;
 	public static CustomBlock oca;
 	public static CustomBlock diaca;
 	public static CustomBlock nca;
 	
 	public static CustomBlock wslc;
 	public static CustomBlock cslc;
 	public static CustomBlock glslc;
 	public static CustomBlock ssslc;
 	public static CustomBlock dirslc;
 	public static CustomBlock stslc;
 	public static CustomBlock saslc;
 	public static CustomBlock snslc;
 	public static CustomBlock grasslc;
 	public static CustomBlock bslc;
 	public static CustomBlock gravslc;
 	public static CustomBlock goslc;
 	public static CustomBlock islc;
 	public static CustomBlock oslc;
 	public static CustomBlock diaslc;
 	public static CustomBlock nslc;
 	
 	public static CustomBlock wcsc;
 	public static CustomBlock ccsc;
 	public static CustomBlock glcsc;
 	public static CustomBlock sscsc;
 	public static CustomBlock dircsc;
 	public static CustomBlock stcsc;
 	public static CustomBlock sacsc;
 	public static CustomBlock sncsc;
 	public static CustomBlock grascsc;
 	public static CustomBlock bcsc;
 	public static CustomBlock gravcsc;
 	public static CustomBlock gocsc;
 	public static CustomBlock icsc;
 	public static CustomBlock ocsc;
 	public static CustomBlock diacsc;
 	public static CustomBlock ncsc;
 	
 	public static CustomBlock wos;
 	public static CustomBlock cos;
 	public static CustomBlock glos;
 	public static CustomBlock ssos;
 	public static CustomBlock diros;
 	public static CustomBlock stos;
 	public static CustomBlock saos;
 	public static CustomBlock snos;
 	public static CustomBlock grasos;
 	public static CustomBlock bos;
 	public static CustomBlock gravos;
 	public static CustomBlock goos;
 	public static CustomBlock ios;
 	public static CustomBlock oos;
 	public static CustomBlock diaos;
 	public static CustomBlock nos;
 	
 	public static CustomBlock wios;
 	public static CustomBlock cios;
 	public static CustomBlock glios;
 	public static CustomBlock ssios;
 	public static CustomBlock dirios;
 	public static CustomBlock stios;
 	public static CustomBlock saios;
 	public static CustomBlock snios;
 	public static CustomBlock grasios;
 	public static CustomBlock bios;
 	public static CustomBlock gravios;
 	public static CustomBlock goios;
 	public static CustomBlock iios;
 	public static CustomBlock oios;
 	public static CustomBlock diaios;
 	public static CustomBlock nios;
 	
 	public static CustomBlock wsi;
 	public static CustomBlock csi;
 	public static CustomBlock glsi;
 	public static CustomBlock sssi;
 	public static CustomBlock dirsi;
 	public static CustomBlock stsi;
 	public static CustomBlock sasi;
 	public static CustomBlock snsi;
 	public static CustomBlock grassi;
 	public static CustomBlock bsi;
 	public static CustomBlock gravsi;
 	public static CustomBlock gosi;
 	public static CustomBlock isi;
 	public static CustomBlock osi;
 	public static CustomBlock diasi;
 	public static CustomBlock nsi;
 	
 	public static CustomBlock wisi;
 	public static CustomBlock cisi;
 	public static CustomBlock glisi;
 	public static CustomBlock ssisi;
 	public static CustomBlock dirisi;
 	public static CustomBlock stisi;
 	public static CustomBlock saisi;
 	public static CustomBlock snisi;
 	public static CustomBlock grasisi;
 	public static CustomBlock bisi;
 	public static CustomBlock gravisi;
 	public static CustomBlock goisi;
 	public static CustomBlock iisi;
 	public static CustomBlock oisi;
 	public static CustomBlock diaisi;
 	public static CustomBlock nisi;
 	
 	public static Slopes instance;
 	
 	@Override
 	public void onDisable() {
 		System.out.println("[Slopes] disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		instance = this;
 		extractFile("Slopes.png", true);
 		setupTextures();
 		setupBlocks();
 		setupRecipes();
 		System.out.println("[Slopes] enabled.");
 	}
 	
 	public void setupTextures() {
 		tex = new Texture(this, "/plugins/Slopes/Slopes.png", 256, 16, 16);
 		wood = tex.getSubTexture(0);
 		cobble = tex.getSubTexture(1);
 		glass = tex.getSubTexture(2);
 		sandstone = tex.getSubTexture(3);
 		dirt = tex.getSubTexture(4);
 		stone = tex.getSubTexture(5);
 		sand = tex.getSubTexture(6);
 		snow = tex.getSubTexture(7);
 		grass = tex.getSubTexture(8);
 		bricks = tex.getSubTexture(9);
 		gravel = tex.getSubTexture(10);
 		gold = tex.getSubTexture(11);
 		iron = tex.getSubTexture(12);
 		obsidian = tex.getSubTexture(13);
 		diamond = tex.getSubTexture(14);
 		netherbrick = tex.getSubTexture(15);
 	}
 	
 	public void setupBlocks() {
 		setupStairs();
 		setupCeilingStairs();
 		setupSlopedFloors();
 		setupSlopedCeilings();
 		setupCorners();
 		setupSlopedAngles();
 		setupCeilingAngles();
 		setupSlantedCorners();
 		setupCeilingSlantedCorners();
 		setupObliqueSlopes();
 		setupInvertedObliqueSlopes();
 		setupSlopesIntersections();
 		setupInvertedSlopesIntersections();
 	}
 	
 	public void setupStairs() {
 		gls = new GlassStairs(this, "Glass Stairs", tex, glass);
 		sss = new SandstoneStairs(this, "Sandstone Stairs", tex, sandstone);
 		dirs = new DirtStairs(this, "Dirt Stairs", tex, dirt);
 		sas = new SandStairs(this, "Sand Stairs", tex, sand);
 		sns = new SnowStairs(this, "Snow Stairs", tex, snow);
 		grss = new GrassStairs(this, "Grass Stairs", tex, grass);
 		gravs = new GravelStairs(this, "Gravel Stairs", tex, gravel);
 		gos = new GoldStairs(this, "Gold Stairs", tex, gold);
 		is = new IronStairs(this, "Iron Stairs", tex, iron);
 		os = new ObsidianStairs(this, "Obisidian Stairs", tex, obsidian);
 		dias = new DiamondStairs(this, "Diamond Stairs", tex, diamond);
 	}
 	
 	public void setupCeilingStairs() {
 		wcs = new WoodenCeilingStairs(this, "Wooden Ceiling Stairs", tex, wood);
 		ccs = new CobbleCeilingStairs(this, "Cobblestone Ceiling Stairs", tex, cobble);
 		glcs = new GlassCeilingStairs(this, "Glass Ceiling Stairs", tex, glass);
 		sscs = new SandstoneCeilingStairs(this, "Sandstone Ceiling Stairs", tex, sandstone);
 		dircs = new DirtCeilingStairs(this, "Dirt Ceiling Stairs", tex, dirt);
 		stcs = new StoneCeilingStairs(this, "Stone Ceiling Stairs", tex, stone);
 		sacs = new SandCeilingStairs(this, "Sand Ceiling Stairs", tex, sand);
 		sncs = new SnowCeilingStairs(this, "Snow Ceiling Stairs", tex, snow);
 		grascs = new GrassCeilingStairs(this, "Grass Ceiling Stairs", tex, grass);
 		bcs = new BricksCeilingStairs(this, "Bricks Ceiling Stairs", tex, bricks);
 		gravcs = new GravelCeilingStairs(this, "Gravel Ceiling Stairs", tex, gravel);
 		gocs = new GoldCeilingStairs(this, "Gold Ceiling Stairs", tex, gold);
 		ics = new IronCeilingStairs(this, "Iron Ceiling Stairs", tex, iron);
 		ocs = new ObsidianCeilingStairs(this, "Obisidian Ceiling Stairs", tex, obsidian);
 		diacs = new DiamondCeilingStairs(this, "Diamond Ceiling Stairs", tex, diamond);
 		ncs = new NetherbrickCeilingStairs(this, "Netherbrick Ceiling Stairs", tex, netherbrick);
 	}
 	
 	public void setupSlopedFloors() {
 		wsf = new WoodenSlopedFloor(this, "Wooden Sloped Floor", tex, wood);
 		csf = new CobbleSlopedFloor(this, "Cobblestone Sloped Floor", tex, cobble);
 		glsf = new GlassSlopedFloor(this, "Glass Sloped Floor", tex, glass);
 		sssf = new SandstoneSlopedFloor(this, "Sandstone Sloped Floor", tex, sandstone);
 		dirsf = new DirtSlopedFloor(this, "Dirt Sloped Floor", tex, dirt);
 		stsf = new StoneSlopedFloor(this, "Stone Sloped Floor", tex, stone);
 		sasf = new SandSlopedFloor(this, "Sand Sloped Floor", tex, sand);
 		snsf = new SnowSlopedFloor(this, "Snow Sloped Floor", tex, snow);
 		grassf = new GrassSlopedFloor(this, "Grass Sloped Floor", tex, grass);
 		bsf = new BricksSlopedFloor(this, "Bricks Sloped Floor", tex, bricks);
 		gravsf = new GravelSlopedFloor(this, "Gravel Sloped Floor", tex, gravel);
 		gosf = new GoldSlopedFloor(this, "Gold Sloped Floor", tex, gold);
 		isf = new IronSlopedFloor(this, "Iron Sloped Floor", tex, iron);
 		osf = new ObsidianSlopedFloor(this, "Obisidian Sloped Floor", tex, obsidian);
 		diasf = new DiamondSlopedFloor(this, "Diamond Sloped Floor", tex, diamond);
 		nsf = new NetherbrickSlopedFloor(this, "Netherbrick Sloped Floor", tex, netherbrick);
 	}
 	
 	public void setupSlopedCeilings() {
 		wsc = new WoodenSlopedCeiling(this, "Wooden Sloped Ceiling", tex, wood);
 		csc = new CobbleSlopedCeiling(this, "Cobblestone Sloped Ceiling", tex, cobble);
 		glsc = new GlassSlopedCeiling(this, "Glass Sloped Ceiling", tex, glass);
 		sssc = new SandstoneSlopedCeiling(this, "Sandstone Sloped Ceiling", tex, sandstone);
 		dirsc = new DirtSlopedCeiling(this, "Dirt Sloped Ceiling", tex, dirt);
 		stsc = new StoneSlopedCeiling(this, "Stone Sloped Ceiling", tex, stone);
 		sasc = new SandSlopedCeiling(this, "Sand Sloped Ceiling", tex, sand);
 		snsc = new SnowSlopedCeiling(this, "Snow Sloped Ceiling", tex, snow);
 		grassc = new GrassSlopedCeiling(this, "Grass Sloped Ceiling", tex, grass);
 		bsc = new BricksSlopedCeiling(this, "Bricks Sloped Ceiling", tex, bricks);
 		gravsc = new GravelSlopedCeiling(this, "Gravel Sloped Ceiling", tex, gravel);
 		gosc = new GoldSlopedCeiling(this, "Gold Sloped Ceiling", tex, gold);
 		isc = new IronSlopedCeiling(this, "Iron Sloped Ceiling", tex, iron);
 		osc = new ObsidianSlopedCeiling(this, "Obisidian Sloped Ceiling", tex, obsidian);
 		diasc = new DiamondSlopedCeiling(this, "Diamond Sloped Ceiling", tex, diamond);
 		nsc = new NetherbrickSlopedCeiling(this, "Netherbrick Sloped Ceiling", tex, netherbrick);
 	}
 	
 	public void setupCorners() {
 		wc = new WoodenCorner(this, "Wooden Corner", tex, wood);
 		cc = new CobbleCorner(this, "Cobblestone Corner", tex, cobble);
 		glc = new GlassCorner(this, "Glass Corner", tex, glass);
 		ssc = new SandstoneCorner(this, "Sandstone Corner", tex, sandstone);
 		dirc = new DirtCorner(this, "Dirt Corner", tex, dirt);
 		stc = new StoneCorner(this, "Stone Corner", tex, stone);
 		sac = new SandCorner(this, "Sand Corner", tex, sand);
 		snc = new SnowCorner(this, "Snow Corner", tex, snow);
 		grasc = new GrassCorner(this, "Grass Corner", tex, grass);
 		bc = new BricksCorner(this, "Bricks Corner", tex, bricks);
 		gravc = new GravelCorner(this, "Gravel Corner", tex, gravel);
 		goc = new GoldCorner(this, "Gold Corner", tex, gold);
 		ic = new IronCorner(this, "Iron Corner", tex, iron);
 		oc = new ObsidianCorner(this, "Obisidian Corner", tex, obsidian);
 		diac = new DiamondCorner(this, "Diamond Corner", tex, diamond);
 		nc = new NetherbrickCorner(this, "Netherbrick Corner", tex, netherbrick);
 	}
 	
 	public void setupSlopedAngles() {
 		wsa = new WoodenSlopedAngle(this, "Wooden Sloped Angle", tex, wood);
 		csa = new CobbleSlopedAngle(this, "Cobblestone Sloped Angle", tex, cobble);
 		glsa = new GlassSlopedAngle(this, "Glass Sloped Angle", tex, glass);
 		sssa = new SandstoneSlopedAngle(this, "Sandstone Sloped Angle", tex, sandstone);
 		dirsa = new DirtSlopedAngle(this, "Dirt Sloped Angle", tex, dirt);
 		stsa = new StoneSlopedAngle(this, "Stone Sloped Angle", tex, stone);
 		sasa = new SandSlopedAngle(this, "Sand Sloped Angle", tex, sand);
 		snsa = new SnowSlopedAngle(this, "Snow Sloped Angle", tex, snow);
 		grassa = new GrassSlopedAngle(this, "Grass Sloped Angle", tex, grass);
 		bsa = new BricksSlopedAngle(this, "Bricks Sloped Angle", tex, bricks);
 		gravsa = new GravelSlopedAngle(this, "Gravel Sloped Angle", tex, gravel);
 		gosa = new GoldSlopedAngle(this, "Gold Sloped Angle", tex, gold);
 		isa = new IronSlopedAngle(this, "Iron Sloped Angle", tex, iron);
 		osa = new ObsidianSlopedAngle(this, "Obisidian Sloped Angle", tex, obsidian);
 		diasa = new DiamondSlopedAngle(this, "Diamond Sloped Angle", tex, diamond);
 		nsa = new NetherbrickSlopedAngle(this, "Netherbrick Sloped Angle", tex, netherbrick);
 	}
 	
 	public void setupCeilingAngles() {
 		wca = new WoodenCeilingAngle(this, "Wooden Ceiling Angle", tex, wood);
 		cca = new CobbleCeilingAngle(this, "Cobblestone Ceiling Angle", tex, cobble);
 		glca = new GlassCeilingAngle(this, "Glass Ceiling Angle", tex, glass);
 		ssca = new SandstoneCeilingAngle(this, "Sandstone Ceiling Angle", tex, sandstone);
 		dirca = new DirtCeilingAngle(this, "Dirt Ceiling Angle", tex, dirt);
 		stca = new StoneCeilingAngle(this, "Stone Ceiling Angle", tex, stone);
 		saca = new SandCeilingAngle(this, "Sand Ceiling Angle", tex, sand);
 		snca = new SnowCeilingAngle(this, "Snow Ceiling Angle", tex, snow);
 		grasca = new GrassCeilingAngle(this, "Grass Ceiling Angle", tex, grass);
 		bca = new BricksCeilingAngle(this, "Bricks Ceiling Angle", tex, bricks);
 		gravca = new GravelCeilingAngle(this, "Gravel Ceiling Angle", tex, gravel);
 		goca = new GoldCeilingAngle(this, "Gold Ceiling Angle", tex, gold);
 		ica = new IronCeilingAngle(this, "Iron Ceiling Angle", tex, iron);
 		oca = new ObsidianCeilingAngle(this, "Obisidian Ceiling Angle", tex, obsidian);
 		diaca = new DiamondCeilingAngle(this, "Diamond Ceiling Angle", tex, diamond);
 		nca = new NetherbrickCeilingAngle(this, "Netherbrick Ceiling Angle", tex, netherbrick);
 	}
 	
 	public void setupSlantedCorners() {
 		wsc = new WoodenSlantedCorner(this, "Wooden Slanted Corner", tex, wood);
 		csc = new CobbleSlantedCorner(this, "Cobblestone Slanted Corner", tex, cobble);
 		glsc = new GlassSlantedCorner(this, "Glass Slanted Corner", tex, glass);
 		sssc = new SandstoneSlantedCorner(this, "Sandstone Slanted Corner", tex, sandstone);
 		dirsc = new DirtSlantedCorner(this, "Dirt Slanted Corner", tex, dirt);
 		stsc = new StoneSlantedCorner(this, "Stone Slanted Corner", tex, stone);
 		sasc = new SandSlantedCorner(this, "Sand Slanted Corner", tex, sand);
 		snsc = new SnowSlantedCorner(this, "Snow Slanted Corner", tex, snow);
 		grassc = new GrassSlantedCorner(this, "Grass Slanted Corner", tex, grass);
 		bsc = new BricksSlantedCorner(this, "Bricks Slanted Corner", tex, bricks);
 		gravsc = new GravelSlantedCorner(this, "Gravel Slanted Corner", tex, gravel);
 		gosc = new GoldSlantedCorner(this, "Gold Slanted Corner", tex, gold);
 		isc = new IronSlantedCorner(this, "Iron Slanted Corner", tex, iron);
 		osc = new ObsidianSlantedCorner(this, "Obisidian Slanted Corner", tex, obsidian);
 		diasc = new DiamondSlantedCorner(this, "Diamond Slanted Corner", tex, diamond);
 		nsc = new NetherbrickSlantedCorner(this, "Netherbrick Slanted Corner", tex, netherbrick);
 	}
 	
 	public void setupCeilingSlantedCorners() {
 		wcsc = new WoodenCeilingSlantedCorner(this, "Wooden Ceiling Slanted Corner", tex, wood);
 		ccsc = new CobbleCeilingSlantedCorner(this, "Cobblestone Ceiling Slanted Corner", tex, cobble);
 		glcsc = new GlassCeilingSlantedCorner(this, "Glass Ceiling Slanted Corner", tex, glass);
 		sscsc = new SandstoneCeilingSlantedCorner(this, "Sandstone Ceiling Slanted Corner", tex, sandstone);
 		dircsc = new DirtCeilingSlantedCorner(this, "Dirt Ceiling Slanted Corner", tex, dirt);
 		stcsc = new StoneCeilingSlantedCorner(this, "Stone Ceiling Slanted Corner", tex, stone);
 		sacsc = new SandCeilingSlantedCorner(this, "Sand Ceiling Slanted Corner", tex, sand);
 		sncsc = new SnowCeilingSlantedCorner(this, "Snow Ceiling Slanted Corner", tex, snow);
 		grascsc = new GrassCeilingSlantedCorner(this, "Grass Ceiling Slanted Corner", tex, grass);
 		bcsc = new BricksCeilingSlantedCorner(this, "Bricks Ceiling Slanted Corner", tex, bricks);
 		gravcsc = new GravelCeilingSlantedCorner(this, "Gravel Ceiling Slanted Corner", tex, gravel);
 		gocsc = new GoldCeilingSlantedCorner(this, "Gold Ceiling Slanted Corner", tex, gold);
 		icsc = new IronCeilingSlantedCorner(this, "Iron Ceiling Slanted Corner", tex, iron);
 		ocsc = new ObsidianCeilingSlantedCorner(this, "Obisidian Ceiling Slanted Corner", tex, obsidian);
 		diacsc = new DiamondCeilingSlantedCorner(this, "Diamond Ceiling Slanted Corner", tex, diamond);
 		ncsc = new NetherbrickCeilingSlantedCorner(this, "Netherbrick Ceiling Slanted Corner", tex, netherbrick);
 	}
 	
 	public void setupObliqueSlopes() {
 		wos = new WoodenObliqueSlope(this, "Wooden Oblique Slope", tex, wood);
 		cos = new CobbleObliqueSlope(this, "Cobblestone Oblique Slope", tex, cobble);
 		glos = new GlassObliqueSlope(this, "Glass Oblique Slope", tex, glass);
 		ssos = new SandstoneObliqueSlope(this, "Sandstone Oblique Slope", tex, sandstone);
 		diros = new DirtObliqueSlope(this, "Dirt Oblique Slope", tex, dirt);
 		stos = new StoneObliqueSlope(this, "Stone Oblique Slope", tex, stone);
 		saos = new SandObliqueSlope(this, "Sand Oblique Slope", tex, sand);
 		snos = new SnowObliqueSlope(this, "Snow Oblique Slope", tex, snow);
 		grasos = new GrassObliqueSlope(this, "Grass Oblique Slope", tex, grass);
 		bos = new BricksObliqueSlope(this, "Bricks Oblique Slope", tex, bricks);
 		gravos = new GravelObliqueSlope(this, "Gravel Oblique Slope", tex, gravel);
 		goos = new GoldObliqueSlope(this, "Gold Oblique Slope", tex, gold);
 		ios = new IronObliqueSlope(this, "Iron Oblique Slope", tex, iron);
 		oos = new ObsidianObliqueSlope(this, "Obisidian Oblique Slope", tex, obsidian);
 		diaos = new DiamondObliqueSlope(this, "Diamond Oblique Slope", tex, diamond);
 		nos = new NetherbrickObliqueSlope(this, "Netherbrick Oblique Slope", tex, netherbrick);
 	}
 	
 	public void setupInvertedObliqueSlopes() {
 		wios = new WoodenInvertedObliqueSlope(this, "Wooden Inverted Oblique Slope", tex, wood);
 		cios = new CobbleInvertedObliqueSlope(this, "Cobblestone Inverted Oblique Slope", tex, cobble);
 		glios = new GlassInvertedObliqueSlope(this, "Glass Inverted Oblique Slope", tex, glass);
 		ssios = new SandstoneInvertedObliqueSlope(this, "Sandstone Inverted Oblique Slope", tex, sandstone);
 		dirios = new DirtInvertedObliqueSlope(this, "Dirt Inverted Oblique Slope", tex, dirt);
 		stios = new StoneInvertedObliqueSlope(this, "Stone Inverted Oblique Slope", tex, stone);
 		saios = new SandInvertedObliqueSlope(this, "Sand Inverted Oblique Slope", tex, sand);
 		snios = new SnowInvertedObliqueSlope(this, "Snow Inverted Oblique Slope", tex, snow);
 		grasios = new GrassInvertedObliqueSlope(this, "Grass Inverted Oblique Slope", tex, grass);
 		bios = new BricksInvertedObliqueSlope(this, "Bricks Inverted Oblique Slope", tex, bricks);
 		gravios = new GravelInvertedObliqueSlope(this, "Gravel Inverted Oblique Slope", tex, gravel);
 		goios = new GoldInvertedObliqueSlope(this, "Gold Inverted Oblique Slope", tex, gold);
 		iios = new IronInvertedObliqueSlope(this, "Iron Inverted Oblique Slope", tex, iron);
 		oios = new ObsidianInvertedObliqueSlope(this, "Obisidian Inverted Oblique Slope", tex, obsidian);
 		diaios = new DiamondInvertedObliqueSlope(this, "Diamond Inverted Oblique Slope", tex, diamond);
 		nios = new NetherbrickInvertedObliqueSlope(this, "Netherbrick Inverted Oblique Slope", tex, netherbrick);
 	}
 	
 	public void setupSlopesIntersections() {
 		wsi = new WoodenSlopesIntersection(this, "Wooden Slopes Intersection", tex, wood);
 		csi = new CobbleSlopesIntersection(this, "Cobblestone Slopes Intersection", tex, cobble);
 		glsi = new GlassSlopesIntersection(this, "Glass Slopes Intersection", tex, glass);
 		sssi = new SandstoneSlopesIntersection(this, "Sandstone Slopes Intersection", tex, sandstone);
 		dirsi = new DirtSlopesIntersection(this, "Dirt Slopes Intersection", tex, dirt);
 		stsi = new StoneSlopesIntersection(this, "Stone Slopes Intersection", tex, stone);
 		sasi = new SandSlopesIntersection(this, "Sand Slopes Intersection", tex, sand);
 		snsi = new SnowSlopesIntersection(this, "Snow Slopes Intersection", tex, snow);
 		grassi = new GrassSlopesIntersection(this, "Grass Slopes Intersection", tex, grass);
 		bsi = new BricksSlopesIntersection(this, "Bricks Slopes Intersection", tex, bricks);
 		gravsi = new GravelSlopesIntersection(this, "Gravel Slopes Intersection", tex, gravel);
 		gosi = new GoldSlopesIntersection(this, "Gold Slopes Intersection", tex, gold);
 		isi = new IronSlopesIntersection(this, "Iron Slopes Intersection", tex, iron);
 		osi = new ObsidianSlopesIntersection(this, "Obisidian Slopes Intersection", tex, obsidian);
 		diasi = new DiamondSlopesIntersection(this, "Diamond Slopes Intersection", tex, diamond);
 		nsi = new NetherbrickSlopesIntersection(this, "Netherbrick Slopes Intersection", tex, netherbrick);
 	}
 	
 	public void setupInvertedSlopesIntersections() {
 		wisi = new WoodenInvertedSlopesIntersection(this, "Wooden Inverted Slopes Intersection", tex, wood);
 		cisi = new CobbleInvertedSlopesIntersection(this, "Cobblestone Inverted Slopes Intersection", tex, cobble);
 		glisi = new GlassInvertedSlopesIntersection(this, "Glass Inverted Slopes Intersection", tex, glass);
 		ssisi = new SandstoneInvertedSlopesIntersection(this, "Sandstone Inverted Slopes Intersection", tex, sandstone);
 		dirisi = new DirtInvertedSlopesIntersection(this, "Dirt Inverted Slopes Intersection", tex, dirt);
 		stisi = new StoneInvertedSlopesIntersection(this, "Stone Inverted Slopes Intersection", tex, stone);
 		saisi = new SandInvertedSlopesIntersection(this, "Sand Inverted Slopes Intersection", tex, sand);
 		snisi = new SnowInvertedSlopesIntersection(this, "Snow Inverted Slopes Intersection", tex, snow);
 		grasisi = new GrassInvertedSlopesIntersection(this, "Grass Inverted Slopes Intersection", tex, grass);
 		bisi = new BricksInvertedSlopesIntersection(this, "Bricks Inverted Slopes Intersection", tex, bricks);
 		gravisi = new GravelInvertedSlopesIntersection(this, "Gravel Inverted Slopes Intersection", tex, gravel);
 		goisi = new GoldInvertedSlopesIntersection(this, "Gold Inverted Slopes Intersection", tex, gold);
 		iisi = new IronInvertedSlopesIntersection(this, "Iron Inverted Slopes Intersection", tex, iron);
 		oisi = new ObsidianInvertedSlopesIntersection(this, "Obisidian Inverted Slopes Intersection", tex, obsidian);
 		diaisi = new DiamondInvertedSlopesIntersection(this, "Diamond Inverted Slopes Intersection", tex, diamond);
 		nisi = new NetherbrickInvertedSlopesIntersection(this, "Netherbrick Inverted Slopes Intersection", tex, netherbrick);
 	}
 	
 	public void setupRecipes() {
 		setupStairsRecipes();
 		setupCeilingStairsRecipes();
 		setupSlopedFloorRecipes();
 		setupSlopedCeilingRecipes();
 		setupCornerRecipes();
 		setupSlopedAngleRecipes();
 		setupCeilingAngleRecipes();
 		setupSlantedCornerRecipes();
 		setupCeilingSlantedCornerRecipes();
 		setupObliqueSlopeRecipes();
 		setupInvertedObliqueSlopeRecipes();
 		setupSlopesIntersectionRecipes();
 		setupInvertedSlopesIntersectionRecipes();
 	}
 	
 	public void setupStairsRecipes() {
 		ItemStack glsis = new SpoutItemStack(gls, 4);
 		SpoutShapedRecipe glsr = new SpoutShapedRecipe(glsis);
		glsr.shape("A  ", "AA ", "AAA");
 		glsr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glsr);
 		
 		ItemStack sssis = new SpoutItemStack(sss, 4);
 		SpoutShapedRecipe sssr = new SpoutShapedRecipe(sssis);
		sssr.shape("A  ", "AA ", "AAA");
 		sssr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sssr);
 		
 		ItemStack dirsis = new SpoutItemStack(dirs, 4);
 		SpoutShapedRecipe dirsr = new SpoutShapedRecipe(dirsis);
		dirsr.shape("A  ", "AA ", "AAA");
 		dirsr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirsr);
 		
 		ItemStack sasis = new SpoutItemStack(sas, 4);
 		SpoutShapedRecipe sasr = new SpoutShapedRecipe(sasis);
		sasr.shape("A  ", "AA ", "AAA");
 		sasr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sasr);
 		
 		ItemStack snsis = new SpoutItemStack(sns, 4);
 		SpoutShapedRecipe snsr = new SpoutShapedRecipe(snsis);
		snsr.shape("A  ", "AA ", "AAA");
 		snsr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snsr);
 		
 		ItemStack grassis = new SpoutItemStack(grss, 4);
 		SpoutShapedRecipe grassr = new SpoutShapedRecipe(grassis);
		grassr.shape("A  ", "AA ", "AAA");
 		grassr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grassr);
 		
 		ItemStack gravsis = new SpoutItemStack(gravs, 4);
 		SpoutShapedRecipe gravsr = new SpoutShapedRecipe(gravsis);
		gravsr.shape("A  ", "AA ", "AAA");
 		gravsr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravsr);
 		
 		ItemStack gosis = new SpoutItemStack(gos, 4);
 		SpoutShapedRecipe gosr = new SpoutShapedRecipe(gosis);
		gosr.shape("A  ", "AA ", "AAA");
 		gosr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gosr);
 		
 		ItemStack isis = new SpoutItemStack(is, 4);
 		SpoutShapedRecipe isr = new SpoutShapedRecipe(isis);
		isr.shape("A  ", "AA ", "AAA");
 		isr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(isr);
 		
 		ItemStack osis = new SpoutItemStack(os, 4);
 		SpoutShapedRecipe osr = new SpoutShapedRecipe(osis);
		osr.shape("A  ", "AA ", "AAA");
 		osr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(osr);
 		
 		ItemStack diasis = new SpoutItemStack(dias, 4);
 		SpoutShapedRecipe diasr = new SpoutShapedRecipe(diasis);
		diasr.shape("A  ", "AA ", "AAA");
 		diasr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diasr);
 	}
 	
 	public void setupCeilingStairsRecipes() {
 		ItemStack wcsis = new SpoutItemStack(wcs, 4);
 		SpoutShapedRecipe wcsr = new SpoutShapedRecipe(wcsis);
 		wcsr.shape(" AA", "AA ", "A  ");
 		wcsr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wcsr);
 
 		ItemStack ccsis = new SpoutItemStack(ccs, 4);
 		SpoutShapedRecipe ccsr = new SpoutShapedRecipe(ccsis);
 		ccsr.shape(" AA", "AA ", "A  ");
 		ccsr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ccsr);
 		
 		ItemStack glcsis = new SpoutItemStack(glcs, 4);
 		SpoutShapedRecipe glcsr = new SpoutShapedRecipe(glcsis);
 		glcsr.shape(" AA", "AA ", "A  ");
 		glcsr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glcsr);
 		
 		ItemStack sscsis = new SpoutItemStack(sscs, 4);
 		SpoutShapedRecipe sscsr = new SpoutShapedRecipe(sscsis);
 		sscsr.shape(" AA", "AA ", "A  ");
 		sscsr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sscsr);
 		
 		ItemStack dircsis = new SpoutItemStack(dircs, 4);
 		SpoutShapedRecipe dircsr = new SpoutShapedRecipe(dircsis);
 		dircsr.shape(" AA", "AA ", "A  ");
 		dircsr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dircsr);
 		
 		ItemStack stcsis = new SpoutItemStack(stcs, 4);
 		SpoutShapedRecipe stcsr = new SpoutShapedRecipe(stcsis);
 		stcsr.shape(" AA", "AA ", "A  ");
 		stcsr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stcsr);
 		
 		ItemStack sacsis = new SpoutItemStack(sacs, 4);
 		SpoutShapedRecipe sacsr = new SpoutShapedRecipe(sacsis);
 		sacsr.shape(" AA", "AA ", "A  ");
 		sacsr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sacsr);
 		
 		ItemStack sncsis = new SpoutItemStack(sncs, 4);
 		SpoutShapedRecipe sncsr = new SpoutShapedRecipe(sncsis);
 		sncsr.shape(" AA", "AA ", "A  ");
 		sncsr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sncsr);
 		
 		ItemStack grascsis = new SpoutItemStack(grascs, 4);
 		SpoutShapedRecipe grascsr = new SpoutShapedRecipe(grascsis);
 		grascsr.shape(" AA", "AA ", "A  ");
 		grascsr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grascsr);
 		
 		ItemStack bcsis = new SpoutItemStack(bcs, 4);
 		SpoutShapedRecipe bcsr = new SpoutShapedRecipe(bcsis);
 		bcsr.shape(" AA", "AA ", "A  ");
 		bcsr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bcsr);
 		
 		ItemStack gravcsis = new SpoutItemStack(gravcs, 4);
 		SpoutShapedRecipe gravcsr = new SpoutShapedRecipe(gravcsis);
 		gravcsr.shape(" AA", "AA ", "A  ");
 		gravcsr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravcsr);
 		
 		ItemStack gocsis = new SpoutItemStack(gocs, 4);
 		SpoutShapedRecipe gocsr = new SpoutShapedRecipe(gocsis);
 		gocsr.shape(" AA", "AA ", "A  ");
 		gocsr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gocsr);
 		
 		ItemStack icsis = new SpoutItemStack(ics, 4);
 		SpoutShapedRecipe icsr = new SpoutShapedRecipe(icsis);
 		icsr.shape(" AA", "AA ", "A  ");
 		icsr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(icsr);
 		
 		ItemStack ocsis = new SpoutItemStack(ocs, 4);
 		SpoutShapedRecipe ocsr = new SpoutShapedRecipe(ocsis);
 		ocsr.shape(" AA", "AA ", "A  ");
 		ocsr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ocsr);
 		
 		ItemStack diacsis = new SpoutItemStack(diacs, 4);
 		SpoutShapedRecipe diacsr = new SpoutShapedRecipe(diacsis);
 		diacsr.shape(" AA", "AA ", "A  ");
 		diacsr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diacsr);
 		
 		ItemStack ncsis = new SpoutItemStack(ncs, 4);
 		SpoutShapedRecipe ncsr = new SpoutShapedRecipe(ncsis);
 		ncsr.shape(" AA", "AA ", "A  ");
 		ncsr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ncsr);
 	}
 	
 	public void setupSlopedFloorRecipes() {
 		ItemStack wsfis = new SpoutItemStack(wsf, 4);
 		SpoutShapedRecipe wsfr = new SpoutShapedRecipe(wsfis);
 		wsfr.shape("A  ", " A ", "AAA");
 		wsfr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wsfr);
 
 		ItemStack csfis = new SpoutItemStack(csf, 4);
 		SpoutShapedRecipe csfr = new SpoutShapedRecipe(csfis);
 		csfr.shape("A  ", " A ", "AAA");
 		csfr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(csfr);
 		
 		ItemStack glsfis = new SpoutItemStack(glsf, 4);
 		SpoutShapedRecipe glsfr = new SpoutShapedRecipe(glsfis);
 		glsfr.shape("A  ", " A ", "AAA");
 		glsfr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glsfr);
 		
 		ItemStack sssfis = new SpoutItemStack(sssf, 4);
 		SpoutShapedRecipe sssfr = new SpoutShapedRecipe(sssfis);
 		sssfr.shape("A  ", " A ", "AAA");
 		sssfr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sssfr);
 		
 		ItemStack dirsfis = new SpoutItemStack(dirsf, 4);
 		SpoutShapedRecipe dirsfr = new SpoutShapedRecipe(dirsfis);
 		dirsfr.shape("A  ", " A ", "AAA");
 		dirsfr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirsfr);
 		
 		ItemStack stsfis = new SpoutItemStack(stsf, 4);
 		SpoutShapedRecipe stsfr = new SpoutShapedRecipe(stsfis);
 		stsfr.shape("A  ", " A ", "AAA");
 		stsfr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stsfr);
 		
 		ItemStack sasfis = new SpoutItemStack(sasf, 4);
 		SpoutShapedRecipe sasfr = new SpoutShapedRecipe(sasfis);
 		sasfr.shape("A  ", " A ", "AAA");
 		sasfr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sasfr);
 		
 		ItemStack snsfis = new SpoutItemStack(snsf, 4);
 		SpoutShapedRecipe snsfr = new SpoutShapedRecipe(snsfis);
 		snsfr.shape("A  ", " A ", "AAA");
 		snsfr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snsfr);
 		
 		ItemStack grassfis = new SpoutItemStack(grassf, 4);
 		SpoutShapedRecipe grassfr = new SpoutShapedRecipe(grassfis);
 		grassfr.shape("A  ", " A ", "AAA");
 		grassfr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grassfr);
 		
 		ItemStack bsfis = new SpoutItemStack(bsf, 4);
 		SpoutShapedRecipe bsfr = new SpoutShapedRecipe(bsfis);
 		bsfr.shape("A  ", " A ", "AAA");
 		bsfr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bsfr);
 		
 		ItemStack gravsfis = new SpoutItemStack(gravsf, 4);
 		SpoutShapedRecipe gravsfr = new SpoutShapedRecipe(gravsfis);
 		gravsfr.shape("A  ", " A ", "AAA");
 		gravsfr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravsfr);
 		
 		ItemStack gosfis = new SpoutItemStack(gosf, 4);
 		SpoutShapedRecipe gosfr = new SpoutShapedRecipe(gosfis);
 		gosfr.shape("A  ", " A ", "AAA");
 		gosfr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gosfr);
 		
 		ItemStack isfis = new SpoutItemStack(isf, 4);
 		SpoutShapedRecipe isfr = new SpoutShapedRecipe(isfis);
 		isfr.shape("A  ", " A ", "AAA");
 		isfr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(isfr);
 		
 		ItemStack osfis = new SpoutItemStack(osf, 4);
 		SpoutShapedRecipe osfr = new SpoutShapedRecipe(osfis);
 		osfr.shape("A  ", " A ", "AAA");
 		osfr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(osfr);
 		
 		ItemStack diasfis = new SpoutItemStack(diasf, 4);
 		SpoutShapedRecipe diasfr = new SpoutShapedRecipe(diasfis);
 		diasfr.shape("A  ", " A ", "AAA");
 		diasfr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diasfr);
 		
 		ItemStack nsfis = new SpoutItemStack(nsf, 4);
 		SpoutShapedRecipe nsfr = new SpoutShapedRecipe(nsfis);
 		nsfr.shape("A  ", " A ", "AAA");
 		nsfr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(nsfr);
 	}
 	
 	public void setupSlopedCeilingRecipes() {
 		ItemStack wscis = new SpoutItemStack(wsc, 4);
 		SpoutShapedRecipe wscr = new SpoutShapedRecipe(wscis);
 		wscr.shape("AAA", " A ", "  A");
 		wscr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wscr);
 
 		ItemStack cscis = new SpoutItemStack(csc, 4);
 		SpoutShapedRecipe cscr = new SpoutShapedRecipe(cscis);
 		cscr.shape("AAA", " A ", "  A");
 		cscr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(cscr);
 		
 		ItemStack glscis = new SpoutItemStack(glsc, 4);
 		SpoutShapedRecipe glscr = new SpoutShapedRecipe(glscis);
 		glscr.shape("AAA", " A ", "  A");
 		glscr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glscr);
 		
 		ItemStack ssscis = new SpoutItemStack(sssc, 4);
 		SpoutShapedRecipe ssscr = new SpoutShapedRecipe(ssscis);
 		ssscr.shape("AAA", " A ", "  A");
 		ssscr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ssscr);
 		
 		ItemStack dirscis = new SpoutItemStack(dirsc, 4);
 		SpoutShapedRecipe dirscr = new SpoutShapedRecipe(dirscis);
 		dirscr.shape("AAA", " A ", "  A");
 		dirscr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirscr);
 		
 		ItemStack stscis = new SpoutItemStack(stsc, 4);
 		SpoutShapedRecipe stscr = new SpoutShapedRecipe(stscis);
 		stscr.shape("AAA", " A ", "  A");
 		stscr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stscr);
 		
 		ItemStack sascis = new SpoutItemStack(sasc, 4);
 		SpoutShapedRecipe sascr = new SpoutShapedRecipe(sascis);
 		sascr.shape("AAA", " A ", "  A");
 		sascr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sascr);
 		
 		ItemStack snscis = new SpoutItemStack(snsc, 4);
 		SpoutShapedRecipe snscr = new SpoutShapedRecipe(snscis);
 		snscr.shape("AAA", " A ", "  A");
 		snscr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snscr);
 		
 		ItemStack grasscis = new SpoutItemStack(grassc, 4);
 		SpoutShapedRecipe grasscr = new SpoutShapedRecipe(grasscis);
 		grasscr.shape("AAA", " A ", "  A");
 		grasscr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grasscr);
 		
 		ItemStack bscis = new SpoutItemStack(bsc, 4);
 		SpoutShapedRecipe bscr = new SpoutShapedRecipe(bscis);
 		bscr.shape("AAA", " A ", "  A");
 		bscr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bscr);
 		
 		ItemStack gravscis = new SpoutItemStack(gravsc, 4);
 		SpoutShapedRecipe gravscr = new SpoutShapedRecipe(gravscis);
 		gravscr.shape("AAA", " A ", "  A");
 		gravscr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravscr);
 		
 		ItemStack goscis = new SpoutItemStack(gosc, 4);
 		SpoutShapedRecipe goscr = new SpoutShapedRecipe(goscis);
 		goscr.shape("AAA", " A ", "  A");
 		goscr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(goscr);
 		
 		ItemStack iscis = new SpoutItemStack(isc, 4);
 		SpoutShapedRecipe iscr = new SpoutShapedRecipe(iscis);
 		iscr.shape("AAA", " A ", "  A");
 		iscr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(iscr);
 		
 		ItemStack oscis = new SpoutItemStack(osc, 4);
 		SpoutShapedRecipe oscr = new SpoutShapedRecipe(oscis);
 		oscr.shape("AAA", " A ", "  A");
 		oscr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(oscr);
 		
 		ItemStack diascis = new SpoutItemStack(diasc, 4);
 		SpoutShapedRecipe diascr = new SpoutShapedRecipe(diascis);
 		diascr.shape("AAA", " A ", "  A");
 		diascr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diascr);
 		
 		ItemStack nscis = new SpoutItemStack(nsc, 4);
 		SpoutShapedRecipe nscr = new SpoutShapedRecipe(nscis);
 		nscr.shape("AAA", " A ", "  A");
 		nscr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(nscr);
 	}
 	
 	public void setupCornerRecipes() {
 		ItemStack wcis = new SpoutItemStack(wc, 4);
 		SpoutShapedRecipe wcr = new SpoutShapedRecipe(wcis);
 		wcr.shape(" A ", " AA", " A ");
 		wcr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wcr);
 
 		ItemStack ccis = new SpoutItemStack(cc, 4);
 		SpoutShapedRecipe ccr = new SpoutShapedRecipe(ccis);
 		ccr.shape(" A ", " AA", " A ");
 		ccr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ccr);
 		
 		ItemStack glcis = new SpoutItemStack(glc, 4);
 		SpoutShapedRecipe glcr = new SpoutShapedRecipe(glcis);
 		glcr.shape(" A ", " AA", " A ");
 		glcr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glcr);
 		
 		ItemStack sscis = new SpoutItemStack(ssc, 4);
 		SpoutShapedRecipe sscr = new SpoutShapedRecipe(sscis);
 		sscr.shape(" A ", " AA", " A ");
 		sscr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sscr);
 		
 		ItemStack dircis = new SpoutItemStack(dirc, 4);
 		SpoutShapedRecipe dircr = new SpoutShapedRecipe(dircis);
 		dircr.shape(" A ", " AA", " A ");
 		dircr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dircr);
 		
 		ItemStack stcis = new SpoutItemStack(stc, 4);
 		SpoutShapedRecipe stcr = new SpoutShapedRecipe(stcis);
 		stcr.shape(" A ", " AA", " A ");
 		stcr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stcr);
 		
 		ItemStack sacis = new SpoutItemStack(sac, 4);
 		SpoutShapedRecipe sacr = new SpoutShapedRecipe(sacis);
 		sacr.shape(" A ", " AA", " A ");
 		sacr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sacr);
 		
 		ItemStack sncis = new SpoutItemStack(snc, 4);
 		SpoutShapedRecipe sncr = new SpoutShapedRecipe(sncis);
 		sncr.shape(" A ", " AA", " A ");
 		sncr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sncr);
 		
 		ItemStack grascis = new SpoutItemStack(grasc, 4);
 		SpoutShapedRecipe grascr = new SpoutShapedRecipe(grascis);
 		grascr.shape(" A ", " AA", " A ");
 		grascr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grascr);
 		
 		ItemStack bcis = new SpoutItemStack(bc, 4);
 		SpoutShapedRecipe bcr = new SpoutShapedRecipe(bcis);
 		bcr.shape(" A ", " AA", " A ");
 		bcr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bcr);
 		
 		ItemStack gravcis = new SpoutItemStack(gravc, 4);
 		SpoutShapedRecipe gravcr = new SpoutShapedRecipe(gravcis);
 		gravcr.shape(" A ", " AA", " A ");
 		gravcr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravcr);
 		
 		ItemStack gocis = new SpoutItemStack(goc, 4);
 		SpoutShapedRecipe gocr = new SpoutShapedRecipe(gocis);
 		gocr.shape(" A ", " AA", " A ");
 		gocr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gocr);
 		
 		ItemStack icis = new SpoutItemStack(ic, 4);
 		SpoutShapedRecipe icr = new SpoutShapedRecipe(icis);
 		icr.shape(" A ", " AA", " A ");
 		icr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(icr);
 		
 		ItemStack ocis = new SpoutItemStack(oc, 4);
 		SpoutShapedRecipe ocr = new SpoutShapedRecipe(ocis);
 		ocr.shape(" A ", " AA", " A ");
 		ocr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ocr);
 		
 		ItemStack diacis = new SpoutItemStack(diac, 4);
 		SpoutShapedRecipe diacr = new SpoutShapedRecipe(diacis);
 		diacr.shape(" A ", " AA", " A ");
 		diacr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diacr);
 		
 		ItemStack ncis = new SpoutItemStack(nc, 4);
 		SpoutShapedRecipe ncr = new SpoutShapedRecipe(ncis);
 		ncr.shape(" A ", " AA", " A ");
 		ncr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ncr);
 	}
 	
 	public void setupSlopedAngleRecipes() {
 		ItemStack wsais = new SpoutItemStack(wsa, 4);
 		SpoutShapedRecipe wsar = new SpoutShapedRecipe(wsais);
 		wsar.shape(" A ", "AAA");
 		wsar.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wsar);
 
 		ItemStack csais = new SpoutItemStack(csa, 4);
 		SpoutShapedRecipe csar = new SpoutShapedRecipe(csais);
 		csar.shape(" A ", "AAA");
 		csar.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(csar);
 		
 		ItemStack glsais = new SpoutItemStack(glsa, 4);
 		SpoutShapedRecipe glsar = new SpoutShapedRecipe(glsais);
 		glsar.shape(" A ", "AAA");
 		glsar.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glsar);
 		
 		ItemStack sssais = new SpoutItemStack(sssa, 4);
 		SpoutShapedRecipe sssar = new SpoutShapedRecipe(sssais);
 		sssar.shape(" A ", "AAA");
 		sssar.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sssar);
 		
 		ItemStack dirsais = new SpoutItemStack(dirsa, 4);
 		SpoutShapedRecipe dirsar = new SpoutShapedRecipe(dirsais);
 		dirsar.shape(" A ", "AAA");
 		dirsar.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirsar);
 		
 		ItemStack stsais = new SpoutItemStack(stsa, 4);
 		SpoutShapedRecipe stsar = new SpoutShapedRecipe(stsais);
 		stsar.shape(" A ", "AAA");
 		stsar.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stsar);
 		
 		ItemStack sasais = new SpoutItemStack(sasa, 4);
 		SpoutShapedRecipe sasar = new SpoutShapedRecipe(sasais);
 		sasar.shape(" A ", "AAA");
 		sasar.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sasar);
 		
 		ItemStack snsais = new SpoutItemStack(snsa, 4);
 		SpoutShapedRecipe snsar = new SpoutShapedRecipe(snsais);
 		snsar.shape(" A ", "AAA");
 		snsar.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snsar);
 		
 		ItemStack grassais = new SpoutItemStack(grassa, 4);
 		SpoutShapedRecipe grassar = new SpoutShapedRecipe(grassais);
 		grassar.shape(" A ", "AAA");
 		grassar.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grassar);
 		
 		ItemStack bsais = new SpoutItemStack(bsa, 4);
 		SpoutShapedRecipe bsar = new SpoutShapedRecipe(bsais);
 		bsar.shape(" A ", "AAA");
 		bsar.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bsar);
 		
 		ItemStack gravsais = new SpoutItemStack(gravsa, 4);
 		SpoutShapedRecipe gravsar = new SpoutShapedRecipe(gravsais);
 		gravsar.shape(" A ", "AAA");
 		gravsar.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravsar);
 		
 		ItemStack gosais = new SpoutItemStack(gosa, 4);
 		SpoutShapedRecipe gosar = new SpoutShapedRecipe(gosais);
 		gosar.shape(" A ", "AAA");
 		gosar.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gosar);
 		
 		ItemStack isais = new SpoutItemStack(isa, 4);
 		SpoutShapedRecipe isar = new SpoutShapedRecipe(isais);
 		isar.shape(" A ", "AAA");
 		isar.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(isar);
 		
 		ItemStack osais = new SpoutItemStack(osa, 4);
 		SpoutShapedRecipe osar = new SpoutShapedRecipe(osais);
 		osar.shape(" A ", "AAA");
 		osar.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(osar);
 		
 		ItemStack diasais = new SpoutItemStack(diasa, 4);
 		SpoutShapedRecipe diasar = new SpoutShapedRecipe(diasais);
 		diasar.shape(" A ", "AAA");
 		diasar.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diasar);
 		
 		ItemStack nsais = new SpoutItemStack(nsa, 4);
 		SpoutShapedRecipe nsar = new SpoutShapedRecipe(nsais);
 		nsar.shape(" A ", "AAA");
 		nsar.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(nsar);
 	}
 	
 	public void setupCeilingAngleRecipes() {
 		ItemStack wcais = new SpoutItemStack(wca, 4);
 		SpoutShapedRecipe wcar = new SpoutShapedRecipe(wcais);
 		wcar.shape("AAA", " A ");
 		wcar.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wcar);
 
 		ItemStack ccais = new SpoutItemStack(cca, 4);
 		SpoutShapedRecipe ccar = new SpoutShapedRecipe(ccais);
 		ccar.shape("AAA", " A ");
 		ccar.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ccar);
 		
 		ItemStack glcais = new SpoutItemStack(glca, 4);
 		SpoutShapedRecipe glcar = new SpoutShapedRecipe(glcais);
 		glcar.shape("AAA", " A ");
 		glcar.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glcar);
 		
 		ItemStack sscais = new SpoutItemStack(ssca, 4);
 		SpoutShapedRecipe sscar = new SpoutShapedRecipe(sscais);
 		sscar.shape("AAA", " A ");
 		sscar.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sscar);
 		
 		ItemStack dircais = new SpoutItemStack(dirca, 4);
 		SpoutShapedRecipe dircar = new SpoutShapedRecipe(dircais);
 		dircar.shape("AAA", " A ");
 		dircar.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dircar);
 		
 		ItemStack stcais = new SpoutItemStack(stca, 4);
 		SpoutShapedRecipe stcar = new SpoutShapedRecipe(stcais);
 		stcar.shape("AAA", " A ");
 		stcar.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stcar);
 		
 		ItemStack sacais = new SpoutItemStack(saca, 4);
 		SpoutShapedRecipe sacar = new SpoutShapedRecipe(sacais);
 		sacar.shape("AAA", " A ");
 		sacar.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sacar);
 		
 		ItemStack sncais = new SpoutItemStack(snca, 4);
 		SpoutShapedRecipe sncar = new SpoutShapedRecipe(sncais);
 		sncar.shape("AAA", " A ");
 		sncar.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sncar);
 		
 		ItemStack grascais = new SpoutItemStack(grasca, 4);
 		SpoutShapedRecipe grascar = new SpoutShapedRecipe(grascais);
 		grascar.shape("AAA", " A ");
 		grascar.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grascar);
 		
 		ItemStack bcais = new SpoutItemStack(bca, 4);
 		SpoutShapedRecipe bcar = new SpoutShapedRecipe(bcais);
 		bcar.shape("AAA", " A ");
 		bcar.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bcar);
 		
 		ItemStack gravcais = new SpoutItemStack(gravca, 4);
 		SpoutShapedRecipe gravcar = new SpoutShapedRecipe(gravcais);
 		gravcar.shape("AAA", " A ");
 		gravcar.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravcar);
 		
 		ItemStack gocais = new SpoutItemStack(goca, 4);
 		SpoutShapedRecipe gocar = new SpoutShapedRecipe(gocais);
 		gocar.shape("AAA", " A ");
 		gocar.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gocar);
 		
 		ItemStack icais = new SpoutItemStack(ica, 4);
 		SpoutShapedRecipe icar = new SpoutShapedRecipe(icais);
 		icar.shape("AAA", " A ");
 		icar.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(icar);
 		
 		ItemStack ocais = new SpoutItemStack(oca, 4);
 		SpoutShapedRecipe ocar = new SpoutShapedRecipe(ocais);
 		ocar.shape("AAA", " A ");
 		ocar.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ocar);
 		
 		ItemStack diacais = new SpoutItemStack(diaca, 4);
 		SpoutShapedRecipe diacar = new SpoutShapedRecipe(diacais);
 		diacar.shape("AAA", " A ");
 		diacar.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diacar);
 		
 		ItemStack ncais = new SpoutItemStack(nca, 4);
 		SpoutShapedRecipe ncar = new SpoutShapedRecipe(ncais);
 		ncar.shape("AAA", " A ");
 		ncar.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ncar);
 	}
 	
 	public void setupSlantedCornerRecipes() {
 		ItemStack wscis = new SpoutItemStack(wsc, 4);
 		SpoutShapedRecipe wscr = new SpoutShapedRecipe(wscis);
 		wscr.shape("AAA", " A ", "  A");
 		wscr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wscr);
 
 		ItemStack cscis = new SpoutItemStack(csc, 4);
 		SpoutShapedRecipe cscr = new SpoutShapedRecipe(cscis);
 		cscr.shape("AAA", " A ", "  A");
 		cscr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(cscr);
 		
 		ItemStack glscis = new SpoutItemStack(glsc, 4);
 		SpoutShapedRecipe glscr = new SpoutShapedRecipe(glscis);
 		glscr.shape("AAA", " A ", "  A");
 		glscr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glscr);
 		
 		ItemStack ssscis = new SpoutItemStack(sssc, 4);
 		SpoutShapedRecipe ssscr = new SpoutShapedRecipe(ssscis);
 		ssscr.shape("AAA", " A ", "  A");
 		ssscr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ssscr);
 		
 		ItemStack dirscis = new SpoutItemStack(dirsc, 4);
 		SpoutShapedRecipe dirscr = new SpoutShapedRecipe(dirscis);
 		dirscr.shape("AAA", " A ", "  A");
 		dirscr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirscr);
 		
 		ItemStack stscis = new SpoutItemStack(stsc, 4);
 		SpoutShapedRecipe stscr = new SpoutShapedRecipe(stscis);
 		stscr.shape("AAA", " A ", "  A");
 		stscr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stscr);
 		
 		ItemStack sascis = new SpoutItemStack(sasc, 4);
 		SpoutShapedRecipe sascr = new SpoutShapedRecipe(sascis);
 		sascr.shape("AAA", " A ", "  A");
 		sascr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sascr);
 		
 		ItemStack snscis = new SpoutItemStack(snsc, 4);
 		SpoutShapedRecipe snscr = new SpoutShapedRecipe(snscis);
 		snscr.shape("AAA", " A ", "  A");
 		snscr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snscr);
 		
 		ItemStack grasscis = new SpoutItemStack(grassc, 4);
 		SpoutShapedRecipe grasscr = new SpoutShapedRecipe(grasscis);
 		grasscr.shape("AAA", " A ", "  A");
 		grasscr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grasscr);
 		
 		ItemStack bscis = new SpoutItemStack(bsc, 4);
 		SpoutShapedRecipe bscr = new SpoutShapedRecipe(bscis);
 		bscr.shape("AAA", " A ", "  A");
 		bscr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bscr);
 		
 		ItemStack gravscis = new SpoutItemStack(gravsc, 4);
 		SpoutShapedRecipe gravscr = new SpoutShapedRecipe(gravscis);
 		gravscr.shape("AAA", " A ", "  A");
 		gravscr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravscr);
 		
 		ItemStack goscis = new SpoutItemStack(gosc, 4);
 		SpoutShapedRecipe goscr = new SpoutShapedRecipe(goscis);
 		goscr.shape("AAA", " A ", "  A");
 		goscr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(goscr);
 		
 		ItemStack iscis = new SpoutItemStack(isc, 4);
 		SpoutShapedRecipe iscr = new SpoutShapedRecipe(iscis);
 		iscr.shape("AAA", " A ", "  A");
 		iscr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(iscr);
 		
 		ItemStack oscis = new SpoutItemStack(osc, 4);
 		SpoutShapedRecipe oscr = new SpoutShapedRecipe(oscis);
 		oscr.shape("AAA", " A ", "  A");
 		oscr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(oscr);
 		
 		ItemStack diascis = new SpoutItemStack(diasc, 4);
 		SpoutShapedRecipe diascr = new SpoutShapedRecipe(diascis);
 		diascr.shape("AAA", " A ", "  A");
 		diascr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diascr);
 		
 		ItemStack nscis = new SpoutItemStack(nsc, 4);
 		SpoutShapedRecipe nscr = new SpoutShapedRecipe(nscis);
 		nscr.shape("AAA", " A ", "  A");
 		nscr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(nscr);
 	}
 	
 	public void setupCeilingSlantedCornerRecipes() {
 		ItemStack wcscis = new SpoutItemStack(wcsc, 4);
 		SpoutShapedRecipe wcscr = new SpoutShapedRecipe(wcscis);
 		wcscr.shape("AAA", " A ", " A ");
 		wcscr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wcscr);
 
 		ItemStack ccscis = new SpoutItemStack(ccsc, 4);
 		SpoutShapedRecipe ccscr = new SpoutShapedRecipe(ccscis);
 		ccscr.shape("AAA", " A ", " A ");
 		ccscr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ccscr);
 		
 		ItemStack glcscis = new SpoutItemStack(glcsc, 4);
 		SpoutShapedRecipe glcscr = new SpoutShapedRecipe(glcscis);
 		glcscr.shape("AAA", " A ", " A ");
 		glcscr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glcscr);
 		
 		ItemStack sscscis = new SpoutItemStack(sscsc, 4);
 		SpoutShapedRecipe sscscr = new SpoutShapedRecipe(sscscis);
 		sscscr.shape("AAA", " A ", " A ");
 		sscscr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sscscr);
 		
 		ItemStack dircscis = new SpoutItemStack(dircsc, 4);
 		SpoutShapedRecipe dircscr = new SpoutShapedRecipe(dircscis);
 		dircscr.shape("AAA", " A ", " A ");
 		dircscr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dircscr);
 		
 		ItemStack stcscis = new SpoutItemStack(stcsc, 4);
 		SpoutShapedRecipe stcscr = new SpoutShapedRecipe(stcscis);
 		stcscr.shape("AAA", " A ", " A ");
 		stcscr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stcscr);
 		
 		ItemStack sacscis = new SpoutItemStack(sacsc, 4);
 		SpoutShapedRecipe sacscr = new SpoutShapedRecipe(sacscis);
 		sacscr.shape("AAA", " A ", " A ");
 		sacscr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sacscr);
 		
 		ItemStack sncscis = new SpoutItemStack(sncsc, 4);
 		SpoutShapedRecipe sncscr = new SpoutShapedRecipe(sncscis);
 		sncscr.shape("AAA", " A ", " A ");
 		sncscr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sncscr);
 		
 		ItemStack grascscis = new SpoutItemStack(grascsc, 4);
 		SpoutShapedRecipe grascscr = new SpoutShapedRecipe(grascscis);
 		grascscr.shape("AAA", " A ", " A ");
 		grascscr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grascscr);
 		
 		ItemStack bcscis = new SpoutItemStack(bcsc, 4);
 		SpoutShapedRecipe bcscr = new SpoutShapedRecipe(bcscis);
 		bcscr.shape("AAA", " A ", " A ");
 		bcscr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bcscr);
 		
 		ItemStack gravcscis = new SpoutItemStack(gravcsc, 4);
 		SpoutShapedRecipe gravcscr = new SpoutShapedRecipe(gravcscis);
 		gravcscr.shape("AAA", " A ", " A ");
 		gravcscr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravcscr);
 		
 		ItemStack gocscis = new SpoutItemStack(gocsc, 4);
 		SpoutShapedRecipe gocscr = new SpoutShapedRecipe(gocscis);
 		gocscr.shape("AAA", " A ", " A ");
 		gocscr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gocscr);
 		
 		ItemStack icscis = new SpoutItemStack(icsc, 4);
 		SpoutShapedRecipe icscr = new SpoutShapedRecipe(icscis);
 		icscr.shape("AAA", " A ", " A ");
 		icscr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(icscr);
 		
 		ItemStack ocscis = new SpoutItemStack(ocsc, 4);
 		SpoutShapedRecipe ocscr = new SpoutShapedRecipe(ocscis);
 		ocscr.shape("AAA", " A ", " A ");
 		ocscr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ocscr);
 		
 		ItemStack diacscis = new SpoutItemStack(diacsc, 4);
 		SpoutShapedRecipe diacscr = new SpoutShapedRecipe(diacscis);
 		diacscr.shape("AAA", " A ", " A ");
 		diacscr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diacscr);
 		
 		ItemStack ncscis = new SpoutItemStack(ncsc, 4);
 		SpoutShapedRecipe ncscr = new SpoutShapedRecipe(ncscis);
 		ncscr.shape("AAA", " A ", " A ");
 		ncscr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ncscr);
 	}
 	
 	public void setupObliqueSlopeRecipes() {
 		ItemStack wosis = new SpoutItemStack(wos, 4);
 		SpoutShapedRecipe wosr = new SpoutShapedRecipe(wosis);
 		wosr.shape("  A", "AAA");
 		wosr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wosr);
 
 		ItemStack cosis = new SpoutItemStack(cos, 4);
 		SpoutShapedRecipe cosr = new SpoutShapedRecipe(cosis);
 		cosr.shape("  A", "AAA");
 		cosr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(cosr);
 		
 		ItemStack glosis = new SpoutItemStack(glos, 4);
 		SpoutShapedRecipe glosr = new SpoutShapedRecipe(glosis);
 		glosr.shape("  A", "AAA");
 		glosr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glosr);
 		
 		ItemStack ssosis = new SpoutItemStack(ssos, 4);
 		SpoutShapedRecipe ssosr = new SpoutShapedRecipe(ssosis);
 		ssosr.shape("  A", "AAA");
 		ssosr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ssosr);
 		
 		ItemStack dirosis = new SpoutItemStack(diros, 4);
 		SpoutShapedRecipe dirosr = new SpoutShapedRecipe(dirosis);
 		dirosr.shape("  A", "AAA");
 		dirosr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirosr);
 		
 		ItemStack stosis = new SpoutItemStack(stos, 4);
 		SpoutShapedRecipe stosr = new SpoutShapedRecipe(stosis);
 		stosr.shape("  A", "AAA");
 		stosr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stosr);
 		
 		ItemStack saosis = new SpoutItemStack(saos, 4);
 		SpoutShapedRecipe saosr = new SpoutShapedRecipe(saosis);
 		saosr.shape("  A", "AAA");
 		saosr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(saosr);
 		
 		ItemStack snosis = new SpoutItemStack(snos, 4);
 		SpoutShapedRecipe snosr = new SpoutShapedRecipe(snosis);
 		snosr.shape("  A", "AAA");
 		snosr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snosr);
 		
 		ItemStack grasosis = new SpoutItemStack(grasos, 4);
 		SpoutShapedRecipe grasosr = new SpoutShapedRecipe(grasosis);
 		grasosr.shape("  A", "AAA");
 		grasosr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grasosr);
 		
 		ItemStack bosis = new SpoutItemStack(bos, 4);
 		SpoutShapedRecipe bosr = new SpoutShapedRecipe(bosis);
 		bosr.shape("  A", "AAA");
 		bosr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bosr);
 		
 		ItemStack gravosis = new SpoutItemStack(gravos, 4);
 		SpoutShapedRecipe gravosr = new SpoutShapedRecipe(gravosis);
 		gravosr.shape("  A", "AAA");
 		gravosr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravosr);
 		
 		ItemStack goosis = new SpoutItemStack(goos, 4);
 		SpoutShapedRecipe goosr = new SpoutShapedRecipe(goosis);
 		goosr.shape("  A", "AAA");
 		goosr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(goosr);
 		
 		ItemStack iosis = new SpoutItemStack(ios, 4);
 		SpoutShapedRecipe iosr = new SpoutShapedRecipe(iosis);
 		iosr.shape("  A", "AAA");
 		iosr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(iosr);
 		
 		ItemStack oosis = new SpoutItemStack(oos, 4);
 		SpoutShapedRecipe oosr = new SpoutShapedRecipe(oosis);
 		oosr.shape("  A", "AAA");
 		oosr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(oosr);
 		
 		ItemStack diaosis = new SpoutItemStack(diaos, 4);
 		SpoutShapedRecipe diaosr = new SpoutShapedRecipe(diaosis);
 		diaosr.shape("  A", "AAA");
 		diaosr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diaosr);
 		
 		ItemStack nosis = new SpoutItemStack(nos, 4);
 		SpoutShapedRecipe nosr = new SpoutShapedRecipe(nosis);
 		nosr.shape("  A", "AAA");
 		nosr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(nosr);
 	}
 	
 	public void setupInvertedObliqueSlopeRecipes() {
 		ItemStack wiosis = new SpoutItemStack(wios, 4);
 		SpoutShapedRecipe wiosr = new SpoutShapedRecipe(wiosis);
 		wiosr.shape("AAA", "  A");
 		wiosr.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wiosr);
 
 		ItemStack ciosis = new SpoutItemStack(cios, 4);
 		SpoutShapedRecipe ciosr = new SpoutShapedRecipe(ciosis);
 		ciosr.shape("AAA", "  A");
 		ciosr.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ciosr);
 		
 		ItemStack gliosis = new SpoutItemStack(glios, 4);
 		SpoutShapedRecipe gliosr = new SpoutShapedRecipe(gliosis);
 		gliosr.shape("AAA", "  A");
 		gliosr.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gliosr);
 		
 		ItemStack ssiosis = new SpoutItemStack(ssios, 4);
 		SpoutShapedRecipe ssiosr = new SpoutShapedRecipe(ssiosis);
 		ssiosr.shape("AAA", "  A");
 		ssiosr.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ssiosr);
 		
 		ItemStack diriosis = new SpoutItemStack(dirios, 4);
 		SpoutShapedRecipe diriosr = new SpoutShapedRecipe(diriosis);
 		diriosr.shape("AAA", "  A");
 		diriosr.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diriosr);
 		
 		ItemStack stiosis = new SpoutItemStack(stios, 4);
 		SpoutShapedRecipe stiosr = new SpoutShapedRecipe(stiosis);
 		stiosr.shape("AAA", "  A");
 		stiosr.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stiosr);
 		
 		ItemStack saiosis = new SpoutItemStack(saios, 4);
 		SpoutShapedRecipe saiosr = new SpoutShapedRecipe(saiosis);
 		saiosr.shape("AAA", "  A");
 		saiosr.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(saiosr);
 		
 		ItemStack sniosis = new SpoutItemStack(snios, 4);
 		SpoutShapedRecipe sniosr = new SpoutShapedRecipe(sniosis);
 		sniosr.shape("AAA", "  A");
 		sniosr.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sniosr);
 		
 		ItemStack grasiosis = new SpoutItemStack(grasios, 4);
 		SpoutShapedRecipe grasiosr = new SpoutShapedRecipe(grasiosis);
 		grasiosr.shape("AAA", "  A");
 		grasiosr.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grasiosr);
 		
 		ItemStack biosis = new SpoutItemStack(bios, 4);
 		SpoutShapedRecipe biosr = new SpoutShapedRecipe(biosis);
 		biosr.shape("AAA", "  A");
 		biosr.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(biosr);
 		
 		ItemStack graviosis = new SpoutItemStack(gravios, 4);
 		SpoutShapedRecipe graviosr = new SpoutShapedRecipe(graviosis);
 		graviosr.shape("AAA", "  A");
 		graviosr.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(graviosr);
 		
 		ItemStack goiosis = new SpoutItemStack(goios, 4);
 		SpoutShapedRecipe goiosr = new SpoutShapedRecipe(goiosis);
 		goiosr.shape("AAA", "  A");
 		goiosr.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(goiosr);
 		
 		ItemStack iiosis = new SpoutItemStack(iios, 4);
 		SpoutShapedRecipe iiosr = new SpoutShapedRecipe(iiosis);
 		iiosr.shape("AAA", "  A");
 		iiosr.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(iiosr);
 		
 		ItemStack oiosis = new SpoutItemStack(oios, 4);
 		SpoutShapedRecipe oiosr = new SpoutShapedRecipe(oiosis);
 		oiosr.shape("AAA", "  A");
 		oiosr.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(oiosr);
 		
 		ItemStack diaiosis = new SpoutItemStack(diaios, 4);
 		SpoutShapedRecipe diaiosr = new SpoutShapedRecipe(diaiosis);
 		diaiosr.shape("AAA", "  A");
 		diaiosr.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diaiosr);
 		
 		ItemStack niosis = new SpoutItemStack(nios, 4);
 		SpoutShapedRecipe niosr = new SpoutShapedRecipe(niosis);
 		niosr.shape("AAA", "  A");
 		niosr.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(niosr);
 	}
 	
 	public void setupSlopesIntersectionRecipes() {
 		ItemStack wsiis = new SpoutItemStack(wsi, 4);
 		SpoutShapedRecipe wsir = new SpoutShapedRecipe(wsiis);
 		wsir.shape("AA ", "A A");
 		wsir.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wsir);
 
 		ItemStack csiis = new SpoutItemStack(csi, 4);
 		SpoutShapedRecipe csir = new SpoutShapedRecipe(csiis);
 		csir.shape("AA ", "A A");
 		csir.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(csir);
 		
 		ItemStack glsiis = new SpoutItemStack(glsi, 4);
 		SpoutShapedRecipe glsir = new SpoutShapedRecipe(glsiis);
 		glsir.shape("AA ", "A A");
 		glsir.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glsir);
 		
 		ItemStack sssiis = new SpoutItemStack(sssi, 4);
 		SpoutShapedRecipe sssir = new SpoutShapedRecipe(sssiis);
 		sssir.shape("AA ", "A A");
 		sssir.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sssir);
 		
 		ItemStack dirsiis = new SpoutItemStack(dirsi, 4);
 		SpoutShapedRecipe dirsir = new SpoutShapedRecipe(dirsiis);
 		dirsir.shape("AA ", "A A");
 		dirsir.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirsir);
 		
 		ItemStack stsiis = new SpoutItemStack(stsi, 4);
 		SpoutShapedRecipe stsir = new SpoutShapedRecipe(stsiis);
 		stsir.shape("AA ", "A A");
 		stsir.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stsir);
 		
 		ItemStack sasiis = new SpoutItemStack(sasi, 4);
 		SpoutShapedRecipe sasir = new SpoutShapedRecipe(sasiis);
 		sasir.shape("AA ", "A A");
 		sasir.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(sasir);
 		
 		ItemStack snsiis = new SpoutItemStack(snsi, 4);
 		SpoutShapedRecipe snsir = new SpoutShapedRecipe(snsiis);
 		snsir.shape("AA ", "A A");
 		snsir.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snsir);
 		
 		ItemStack grassiis = new SpoutItemStack(grassi, 4);
 		SpoutShapedRecipe grassir = new SpoutShapedRecipe(grassiis);
 		grassir.shape("AA ", "A A");
 		grassir.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grassir);
 		
 		ItemStack bsiis = new SpoutItemStack(bsi, 4);
 		SpoutShapedRecipe bsir = new SpoutShapedRecipe(bsiis);
 		bsir.shape("AA ", "A A");
 		bsir.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bsir);
 		
 		ItemStack gravsiis = new SpoutItemStack(gravsi, 4);
 		SpoutShapedRecipe gravsir = new SpoutShapedRecipe(gravsiis);
 		gravsir.shape("AA ", "A A");
 		gravsir.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravsir);
 		
 		ItemStack gosiis = new SpoutItemStack(gosi, 4);
 		SpoutShapedRecipe gosir = new SpoutShapedRecipe(gosiis);
 		gosir.shape("AA ", "A A");
 		gosir.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gosir);
 		
 		ItemStack isiis = new SpoutItemStack(isi, 4);
 		SpoutShapedRecipe isir = new SpoutShapedRecipe(isiis);
 		isir.shape("AA ", "A A");
 		isir.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(isir);
 		
 		ItemStack osiis = new SpoutItemStack(osi, 4);
 		SpoutShapedRecipe osir = new SpoutShapedRecipe(osiis);
 		osir.shape("AA ", "A A");
 		osir.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(osir);
 		
 		ItemStack diasiis = new SpoutItemStack(diasi, 4);
 		SpoutShapedRecipe diasir = new SpoutShapedRecipe(diasiis);
 		diasir.shape("AA ", "A A");
 		diasir.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diasir);
 		
 		ItemStack nsiis = new SpoutItemStack(nsi, 4);
 		SpoutShapedRecipe nsir = new SpoutShapedRecipe(nsiis);
 		nsir.shape("AA ", "A A");
 		nsir.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(nsir);
 	}
 
 	public void setupInvertedSlopesIntersectionRecipes() {
 		ItemStack wisiis = new SpoutItemStack(wisi, 4);
 		SpoutShapedRecipe wisir = new SpoutShapedRecipe(wisiis);
 		wisir.shape("A A", "AA ");
 		wisir.setIngredient('A', MaterialData.wood);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(wisir);
 
 		ItemStack cisiis = new SpoutItemStack(cisi, 4);
 		SpoutShapedRecipe cisir = new SpoutShapedRecipe(cisiis);
 		cisir.shape("A A", "AA ");
 		cisir.setIngredient('A', MaterialData.cobblestone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(cisir);
 		
 		ItemStack glisiis = new SpoutItemStack(glisi, 4);
 		SpoutShapedRecipe glisir = new SpoutShapedRecipe(glisiis);
 		glisir.shape("A A", "AA ");
 		glisir.setIngredient('A', MaterialData.glass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(glisir);
 		
 		ItemStack ssisiis = new SpoutItemStack(ssisi, 4);
 		SpoutShapedRecipe ssisir = new SpoutShapedRecipe(ssisiis);
 		ssisir.shape("A A", "AA ");
 		ssisir.setIngredient('A', MaterialData.sandstone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(ssisir);
 		
 		ItemStack dirisiis = new SpoutItemStack(dirisi, 4);
 		SpoutShapedRecipe dirisir = new SpoutShapedRecipe(dirisiis);
 		dirisir.shape("A A", "AA ");
 		dirisir.setIngredient('A', MaterialData.dirt);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(dirisir);
 		
 		ItemStack stisiis = new SpoutItemStack(stisi, 4);
 		SpoutShapedRecipe stisir = new SpoutShapedRecipe(stisiis);
 		stisir.shape("A A", "AA ");
 		stisir.setIngredient('A', MaterialData.stone);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(stisir);
 		
 		ItemStack saisiis = new SpoutItemStack(saisi, 4);
 		SpoutShapedRecipe saisir = new SpoutShapedRecipe(saisiis);
 		saisir.shape("A A", "AA ");
 		saisir.setIngredient('A', MaterialData.sand);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(saisir);
 		
 		ItemStack snisiis = new SpoutItemStack(snisi, 4);
 		SpoutShapedRecipe snisir = new SpoutShapedRecipe(snisiis);
 		snisir.shape("A A", "AA ");
 		snisir.setIngredient('A', MaterialData.snow);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(snisir);
 		
 		ItemStack grasisiis = new SpoutItemStack(grasisi, 4);
 		SpoutShapedRecipe grasisir = new SpoutShapedRecipe(grasisiis);
 		grasisir.shape("A A", "AA ");
 		grasisir.setIngredient('A', MaterialData.grass);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(grasisir);
 		
 		ItemStack bisiis = new SpoutItemStack(bisi, 4);
 		SpoutShapedRecipe bisir = new SpoutShapedRecipe(bisiis);
 		bisir.shape("A A", "AA ");
 		bisir.setIngredient('A', MaterialData.brick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(bisir);
 		
 		ItemStack gravisiis = new SpoutItemStack(gravisi, 4);
 		SpoutShapedRecipe gravisir = new SpoutShapedRecipe(gravisiis);
 		gravisir.shape("A A", "AA ");
 		gravisir.setIngredient('A', MaterialData.gravel);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(gravisir);
 		
 		ItemStack goisiis = new SpoutItemStack(goisi, 4);
 		SpoutShapedRecipe goisir = new SpoutShapedRecipe(goisiis);
 		goisir.shape("A A", "AA ");
 		goisir.setIngredient('A', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(goisir);
 		
 		ItemStack iisiis = new SpoutItemStack(iisi, 4);
 		SpoutShapedRecipe iisir = new SpoutShapedRecipe(iisiis);
 		iisir.shape("A A", "AA ");
 		iisir.setIngredient('A', MaterialData.ironBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(iisir);
 		
 		ItemStack oisiis = new SpoutItemStack(oisi, 4);
 		SpoutShapedRecipe oisir = new SpoutShapedRecipe(oisiis);
 		oisir.shape("A A", "AA ");
 		oisir.setIngredient('A', MaterialData.obsidian);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(oisir);
 		
 		ItemStack diaisiis = new SpoutItemStack(diaisi, 4);
 		SpoutShapedRecipe diaisir = new SpoutShapedRecipe(diaisiis);
 		diaisir.shape("A A", "AA ");
 		diaisir.setIngredient('A', MaterialData.diamondBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(diaisir);
 		
 		ItemStack nisiis = new SpoutItemStack(nisi, 4);
 		SpoutShapedRecipe nisir = new SpoutShapedRecipe(nisiis);
 		nisir.shape("A A", "AA ");
 		nisir.setIngredient('A', MaterialData.netherBrick);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(nisir);
 	}
 	
 	//Code taken from Rycochet
 	public boolean extractFile(String regex, boolean cache) {
 		boolean found = false;
 		try {
 			JarFile jar = new JarFile(getFile());
 			for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
 				JarEntry entry = (JarEntry) entries.nextElement();
 				String name = entry.getName();
 				if (name.matches(regex)) {
 					if (!getDataFolder().exists()) {
 						getDataFolder().mkdir();
 					}
 					try {
 						File file = new File(getDataFolder(), name);
 						if (!file.exists()) {
 							InputStream is = jar.getInputStream(entry);
 							FileOutputStream fos = new FileOutputStream(file);
 							while (is.available() > 0) {
 								fos.write(is.read());
 							}
 							fos.close();
 							is.close();
 							found = true;
 						}
 						if (cache && name.matches(".*\\.(txt|yml|xml|png|jpg|ogg|midi|wav|zip)$")) {
 							SpoutManager.getFileManager().addToPreLoginCache(this, file);
 						}
 					} catch (Exception e) {
 					}
 				}
 			}
 		} catch (Exception e) {
 		}
 		return found;
 	}
 	
 }
