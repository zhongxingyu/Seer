 package net.nabaal.majiir.realtimerender.palette;
 
 import java.awt.Color;
 import java.util.Random;
 
 import org.bukkit.block.Biome;
 
 public class VariedMaterialColor implements MaterialColor {
 
 	private final MaterialColor source;
 	private final int lightVariance;
 	private final int colorVariance;
 	
 	public VariedMaterialColor(MaterialColor source, int colorVariance, int lightVariance) {
 		this.source = source;
 		this.colorVariance = colorVariance;
 		this.lightVariance = lightVariance;
 	}
 
 	public Color getColor(int x, int z, Color color) {
 		if (color == null) { return null; }
 		final int prime = 29;
 		int hash = 1;
 		hash = prime * hash + x;
 		hash = prime * hash + z;
 		Random random = new Random(hash);
		int mod = random.nextInt(colorVariance * 2 + 1) - colorVariance;
		int r = color.getRed() + mod + nextLightVariance(random);
		int g = color.getGreen() + mod + nextLightVariance(random);
		int b = color.getBlue() + mod + nextLightVariance(random);
 		return new Color(r, g, b, color.getAlpha());
 	}
 	
	private int nextLightVariance(Random random) {
		return random.nextInt(lightVariance * 2 + 1) - lightVariance;
 	}
 	
 	@Override
 	public final Color getColor(int data, int x, int z, double rainfall, double temperature, Biome biome) {
 		return getColor(x, z, source.getColor(data, x, z, rainfall, temperature, biome));
 	}
 
 }
