 package net.mcforge.world.generator;
 
 import java.util.Random;
 
 import net.mcforge.server.Server;
 import net.mcforge.world.Block;
 import net.mcforge.world.Generator;
 import net.mcforge.world.Level;
 
 public class Island implements Generator {
 
 	final Random rand = new Random();
 	float divide;
 	float[] terrain;
 	float[] overlay;
 	float[] overlay2;
 	private Server _server;
 
 	/**
 	 * The constructor for the flatgrass generator
 	 * @param server
 	 *              The server the level is in
 	 */
 	public Island(Server server) {
 		this._server = server;
 	}
 
 	@Override
 	public void generate(Level l) {
 		generate(l, 64, 64, 64);
 	}
 
 	@Override
 	public void generate(Level l, int sizex, int sizey, int sizez) {
 		terrain = new float[l.width * l.height];
 		overlay = new float[l.width * l.height];
 		overlay2 = new float[l.width * l.height]; 
 		short WaterLevel = (short)(l.depth / 2 + 2);
 
 		GenerateFault(terrain, l);
 		FilterAverage(l);
 		GeneratePerlinNoise(overlay, l, rand);
 
 		float RangeLow = 0.4f;
 		float RangeHigh = 0.75f;
 		float TreeDens = 0.35f;
 		short TreeDist = 3;
 		try {
 			for (int bb = 0; bb < terrain.length; bb++)
 			{
 				short x = (short)(bb % l.width);
 				short y = (short)(bb / l.width);
 				short z = Evaluate(l, Range(terrain[bb], RangeLow - NegateEdge(x, y, l), RangeHigh - NegateEdge(x, y, l)));
 				if (z > WaterLevel)
 				{
 					for (short zz = 0; z - zz >= 0; zz++)
 					{
 						if (overlay[bb] < 0.72f)    //If not zoned for rocks or gravel
 						{
 							if (z > WaterLevel + 2)
 							{
 								if (zz == 0) { l.skipChange(x, z - zz, y, Block.getBlock("Grass"), _server); }      //top layer
 								else if (zz < 3) { l.skipChange(x, (short)(z - zz), y, Block.getBlock("Dirt"), _server); }   //next few
 								else { l.skipChange(x, (short)(z - zz), y, Block.getBlock("Stone"), _server); }               //ten rock it
 							}
 							else
 							{
 								l.skipChange(x, (short)(z - zz), y, Block.getBlock("Sand"), _server);                        //SAAAND extra for islands
 							}
 						}
 						else
 						{
 							l.skipChange(x, (short)(z - zz), y, Block.getBlock("Stone"), _server);    //zoned for above sea level rock floor
 						}
 					}
 					int temprand = rand.nextInt(12);
 
 					switch (temprand)
 					{
 					case 10:
 						l.skipChange(x, (short)(z + 1), y, Block.getBlock("RedFlower"), _server);
 						break;
 					case 11:
 						l.skipChange(x, (short)(z + 1), y, Block.getBlock("YellowFlower"), _server);
 						break;
 					default:
 						break;
 					}
 					if (overlay[bb] < 0.65f && overlay2[bb] < TreeDens)
 					{
 						if (l.getTile(x, z + 1, y).getVisableBlock() == (byte)0)
 						{
 							if (l.getTile(x, z, y).name.equals("Grass"))
 							{
 								if (rand.nextInt(13) == 0)
 								{
 									if (!TreeCheck(l, x, z, y, TreeDist))
 									{
 										AddTree(l, x, (short)(z + 1), y, rand);
 									}
 								}
 							}
 						}
 					}
 
 				}
 				else    //Must be on/under the water line then
 				{
 					for (short zz = 0; WaterLevel - zz >= 0; zz++)
 					{
 						if (WaterLevel - zz > z)
 						{ l.skipChange(x, (short)(WaterLevel - zz), y, Block.getBlock("Water"), _server); }    //better fill the water aboce me
 						else if (WaterLevel - zz > z - 3)
 						{
 							if (overlay[bb] < 0.75f)
 							{
 								l.skipChange(x, (short)(WaterLevel - zz), y, Block.getBlock("Sand"), _server);   //sand top
 							}
 							else
 							{
 								l.skipChange(x, (short)(WaterLevel - zz), y, Block.getBlock("Gravel"), _server);  //zoned for gravel
 							}
 						}
 						else
 						{ 
 							l.skipChange(x, (short)(WaterLevel - zz), y, Block.getBlock("Stone"), _server); 
 						}
 					}
 				}
 			}
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		terrain = new float[0]; //Derp
 		overlay = new float[0]; //Derp
 		overlay2 = new float[0]; //Derp
 	}
 
 	void AddTree(Level Lvl, short x, short y, short z, Random Rand)
 	{
		byte height = (byte)(5 + (int)(Math.random() * ((8 - 5) + 1)));
 		for (short yy = 0; yy < height; yy++) Lvl.skipChange(x, (short)(y + yy), z, Block.getBlock("Wood"), _server);
 
		short top = (short)(height - (2 + (int)(Math.random() * ((4 - 2) + 1))));
 
 		for (short xx = (short)-top; xx <= top; ++xx)
 		{
 			for (short yy = (short)-top; yy <= top; ++yy)
 			{
 				for (short zz = (short)-top; zz <= top; ++zz)
 				{
 					short Dist = (short)(Math.sqrt(xx * xx + yy * yy + zz * zz));
 					if (Dist < top + 1)
 					{
						try {
							if (Rand.nextInt((int)(Dist)) < 2)
 							{
 								Lvl.skipChange((short)(x + xx), (short)(y + yy + height), (short)(z + zz), Block.getBlock("Leaves"), _server);
 							}
						} catch (Exception e) { }
 					}
 				}
 			}
 		}
 	}
 	private boolean TreeCheck(Level Lvl, short x, short z, short y, short dist)         //return true if tree is near
 	{
 		for (short xx = (short)-dist; xx <= +dist; ++xx)
 		{
 			for (short yy = (short)-dist; yy <= +dist; ++yy)
 			{
 				for (short zz = (short)-dist; zz <= +dist; ++zz)
 				{
 					byte foundTile = Lvl.getTile((short)(x + xx), (short)(z + zz), (short)(y + yy)).getVisableBlock();
 					if (foundTile == Block.getBlock("Wood").getVisableBlock() || foundTile == Block.getBlock("Green").getVisableBlock())
 					{
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private void GenerateFault(float[] array, Level l) {
 		float startheight = 0.5f;
 		float dispAux;
 		short i, j, k, halfX, halfZ;
 		float a, b, c, w, d;
 
 		float DispMax, DispMin, DispChange;
 		DispMax = 0.01f;
 		DispChange = -0.0025f;
 
 		for (int x = 0; x < array.length; x++)
 		{
 			array[x] = startheight;
 		}
 		DispMin = -DispMax;
 		float disp = DispMax;
 
 
 		halfX = (short)(l.width / 2);
 		halfZ = (short)(l.height / 2);
 		int numIterations = (int)((l.width + l.height));
 		for (k = 0; k < numIterations; k++)
 		{
 			//s.Log("itteration " + k.ToString());
 			d = (float)Math.sqrt(halfX * halfX + halfZ * halfZ);
 			w = (float)(rand.nextDouble() * 360);
 			//w = (float)(rand.NextDouble()*90);
 			a = (float)Math.cos(w);
 			b = (float)Math.sin(w);
 
 			c = ((float)rand.nextDouble()) * 2 * d - d;
 			for (i = 0; i < l.height; i++)
 			{
 				for (j = 0; j < l.width; j++)
 				{
 					if ((i - halfZ) * a + (j - halfX) * b + c > 0)
 						dispAux = disp;
 					else
 						dispAux = -disp;
 					AddTerrainHeight(array, j, i, l.width, dispAux);
 				}
 			}
 
 			disp += DispChange;
 			if (disp < DispMin) { disp = DispMax; }
 		}
 	}
 	void AddTerrainHeight(float[] array, short x, short y, short width, float height)
 	{
 		int temp = x + y * width;
 		if (temp < 0) return;
 		if (temp > array.length) return;
 
 		array[temp] += height;
 
 		if (array[temp] > 1.0f) array[temp] = 1.0f;
 		if (array[temp] < 0.0f) array[temp] = 0.0f;
 	}
 
 	void FilterAverage(Level l)
 	{
 
 		float[] filtered = new float[terrain.length];
 
 		for (int bb = 0; bb < terrain.length; bb++)
 		{
 			short x = (short)(bb % l.width);
 			short y = (short)(bb / l.width);
 			filtered[bb] = GetAverage9(x, y, l);
 		}
 
 		for (int bb = 0; bb < terrain.length; bb++)
 		{
 			terrain[bb] = filtered[bb];
 		}
 	}
 	//Averages over 5 points
 	float GetAverage5(short x, short y, Level l)
 	{
 		divide = 0.0f;
 		float temp = GetPixel(x, y, l);
 		temp += GetPixel((short)(x + 1), y, l);
 		temp += GetPixel((short)(x - 1), y, l);
 		temp += GetPixel(x, (short)(y + 1), l);
 		temp += GetPixel(x, (short)(y - 1), l);
 
 		return temp / divide;
 	}
 	//Averages over 9 points
 	float GetAverage9(short x, short y, Level l)
 	{
 		divide = 0.0f;
 		float temp = GetPixel(x, y, l);
 		temp += GetPixel((short)(x + 1), y, l);
 		temp += GetPixel((short)(x - 1), y, l);
 		temp += GetPixel(x, (short)(y + 1), l);
 		temp += GetPixel(x, (short)(y - 1), l);
 
 		temp += GetPixel((short)(x + 1), (short)(y + 1), l);
 		temp += GetPixel((short)(x - 1), (short)(y + 1), l);
 		temp += GetPixel((short)(x + 1), (short)(y - 1), l);
 		temp += GetPixel((short)(x - 1), (short)(y - 1), l);
 
 		return temp / divide;
 	}
 
 	float GetPixel(short x, short y, Level l)
 	{
 		if (x < 0) { return 0.0f; }
 		if (x >= l.width) { return 0.0f; }
 		if (y < 0) { return 0.0f; }
 		if (y >= l.height) { return 0.0f; }
 		divide += 1.0f;
 		return terrain[x + y * l.width];
 	}
 
 	float Range(float input, float low, float high)
 	{
 		if (high <= low) { return low; }
 		return low + (input * (high - low));
 	}
 
 	//Forces the edge of a map to slope lower for island map types
 	float NegateEdge(short x, short y, Level l)
 	{
 		float tempx = 0.0f, tempy = 0.0f;
 		float temp;
 		if (x != 0) { tempx = ((float)x / (float)l.width) * 0.5f; }
 		if (y != 0) { tempy = ((float)y / (float)l.height) * 0.5f; }
 		tempx = Math.abs(tempx - 0.25f);
 		tempy = Math.abs(tempy - 0.25f);
 		if (tempx > tempy)
 		{
 			temp = tempx - 0.15f;
 		}
 		else
 		{
 			temp = tempy - 0.15f;
 		}
 
 		//s.Log("temp = " + temp.ToString());
 		if (temp > 0.0f) { return temp; }
 		return 0.0f;
 	}
 
 	void GeneratePerlinNoise(float[] array, Level l, Random rand)
 	{
 		GenerateNormalized(array, 0.7f, 8, l.width, l.height, rand.nextInt(), 64);
 	}
 
 	void GenerateNormalized(float[] array, float persistence, int octaves, int width, int height, int seed, float zoom)
 	{
 		float min = 0;
 		float max = 0;
 		for (int y = 0; y < height; ++y)
 		{
 			for (int x = 0; x < width; ++x)
 			{
 				float total = 0;
 				float frequency = 1;
 				float amplitude = 1;
 
 				for (int i = 0; i < octaves; ++i)
 				{
 					total = total + InterpolatedNoise(x * frequency / zoom, y * frequency / zoom, seed) * amplitude;
 					frequency *= 2;
 					amplitude *= persistence;
 				}
 
 				array[y * width + x] = total;
 
 				min = total < min ? total : min;
 				max = total > max ? total : max;
 			}
 		}
 
 		//Normalize
 		for (int i = 0; i < width * height; ++i)
 		{
 			array[i] = (array[i] - min) / (max - min);
 		}
 	}
 
 	float Noise(int x, int y, int seed)
 	{
 		int n = x + y * 57 + seed;
 		n = (n << 13) ^ n;
 		return (float)(1.0 - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
 	}
 
 	float SmoothNoise(int x, int y, int seed)
 	{
 		float corners = (Noise(x - 1, y - 1, seed) + Noise(x + 1, y - 1, seed) + Noise(x - 1, y + 1, seed) + Noise(x + 1, y + 1, seed)) / 16;
 		float sides = (Noise(x - 1, y, seed) + Noise(x + 1, y, seed) + Noise(x, y - 1, seed) + Noise(x, y + 1, seed) / 8);
 		float center = Noise(x, y, seed) / 4;
 		return corners + sides + center;
 	}
 
 	float Interpolate(float a, float b, float x)
 	{
 		float ft = x * 3.1415927f;
 		float f = (float)(1 - Math.cos(ft)) * .5f;
 
 		return a * (1 - f) + b * f;
 	}
 
 	float InterpolatedNoise(float x, float y, int seed)
 	{
 		int wholePartX = (int)x;
 		float fractionPartX = x - wholePartX;
 
 		int wholePartY = (int)y;
 		float fractionPartY = y - wholePartY;
 
 		float v1 = SmoothNoise(wholePartX, wholePartY, seed);
 		float v2 = SmoothNoise(wholePartX + 1, wholePartY, seed);
 		float v3 = SmoothNoise(wholePartX, wholePartY + 1, seed);
 		float v4 = SmoothNoise(wholePartX + 1, wholePartY + 1, seed);
 
 		float i1 = Interpolate(v1, v2, fractionPartX);
 		float i2 = Interpolate(v3, v4, fractionPartX);
 
 		return Interpolate(i1, i2, fractionPartY);
 	}
 
 	short Evaluate(Level lvl, float height)
 	{
 		short temp = (short)(height * lvl.depth);
 		if (temp < 0) return 0;
 		if (temp > lvl.depth - 1) return (short)(lvl.depth - 1);
 		return temp;
 	}
 
 }
