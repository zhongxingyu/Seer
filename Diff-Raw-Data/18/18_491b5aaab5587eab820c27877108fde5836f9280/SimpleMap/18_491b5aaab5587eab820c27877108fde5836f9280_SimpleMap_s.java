 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Eric
  * Date: 1/29/12
  * Time: 1:34 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SimpleMap {
     private int mapWidth;
     private int mapHeight;
     private Tile[][] mapTiles;
     private QuadTreeNode[][] mapTreeRoots = new QuadTreeNode[2][1];
 
     private int baseRenderingScalar = 2;
 
     private int multiplier = 10;
 
     public SimpleMap(int mapWidth, int mapHeight) {
         this.mapWidth = mapWidth;
         this.mapHeight = mapHeight;
         mapTiles = new Tile[mapWidth][mapHeight];
     }
 
     /**
      * Takes various parameters and creates a random perlin noise map based on them
      * @param waterLevel the top of the water threshold in the perlin heightmap
      * @param grassLevel the top of the grass threshold in the perlin heightmap
      * @param treeLevel the top of the tree threshold in the perlin heightmap
      * @param mountainLevel the top of the mountain threshold in the perlin heightmap
      */
     public void createMapFromPerlinNoise(int waterLevel, int grassLevel, int treeLevel, int mountainLevel){
         QuadTreeNode[][] baseTreeTiles =  new QuadTreeNode[mapWidth][mapHeight];;
         float[][] finalNoiseArray = PerlinNoise.GeneratePerlinNoise(mapWidth, mapHeight, 8);
         BufferedImage debugImage = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_4BYTE_ABGR);
         for(int y = 0; y < mapHeight; y++){
             for(int x = 0; x < mapWidth; x++){
                 int grey = (int)(255 * finalNoiseArray[x][y]);
                 if(grey < waterLevel){
                     Tile tempWaterTile = new Tile(false, false, "C:/Users/Simop/Desktop/AdvanceWarsClone/Sprites/waterSprite.png", TileTypes.Water);
                     mapTiles[x][y] = tempWaterTile;
                     baseTreeTiles[x][y] = new QuadTreeNode(new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), tempWaterTile, baseRenderingScalar, x, y);
                     debugImage.setRGB(x, y, new Color(0, 0, 255).getRGB());
                 }else if(grey > waterLevel && grey < grassLevel){
                     Tile tempGrassTile = new Tile(true, false, "C:/Users/Simop/Desktop/AdvanceWarsClone/Sprites/grassSprite.png", TileTypes.Grass);
                     mapTiles[x][y] = tempGrassTile;
                     baseTreeTiles[x][y] = new QuadTreeNode(new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), tempGrassTile, baseRenderingScalar, x, y);
                     debugImage.setRGB(x, y, new Color(0, 255, 0).getRGB());
                 }else if(grey > grassLevel && grey < treeLevel){
                     Tile tempTreeTile = new Tile(true, true, "C:/Users/Simop/Desktop/AdvanceWarsClone/Sprites/treeSprite.png", TileTypes.Trees);
                     mapTiles[x][y] = tempTreeTile;
                     baseTreeTiles[x][y] = new QuadTreeNode(new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), tempTreeTile, baseRenderingScalar, x, y);
                     debugImage.setRGB(x, y, new Color(50, 150, 50).getRGB());
                 }else {
                     Tile tempMountainTile = new Tile(false, true, "C:/Users/Simop/Desktop/AdvanceWarsClone/Sprites/mountainSprite.png", TileTypes.Mountains);
                     mapTiles[x][y] = tempMountainTile;
                     baseTreeTiles[x][y] = new QuadTreeNode(new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), new QuadTreeNode(), tempMountainTile, baseRenderingScalar, x, y);
                     debugImage.setRGB(x, y, new Color(150, 100, 25).getRGB());
                 }
             }
         }
         mapTreeRoots = generateQuadTree(baseTreeTiles, mapWidth, mapHeight, 2);
         try{
             ImageIO.write(debugImage, "png", new File("C:/Users/Simop/Desktop/AdvanceWarsClone/Sprites/perlinNoise.png"));
         }catch(IOException e){
             e.printStackTrace();
         }
     }
 
     private QuadTreeNode[][] generateQuadTree(QuadTreeNode[][] nodes, int width, int height, int scalar){
        Tile nullTile = new Tile();
         if(width == 2 && height == 1){
             return nodes;
         }else{
            QuadTreeNode[][] tempNodes = new QuadTreeNode[width / 2][height / 2];
             int tempXindex = 0;
             int tempYindex = 0;
             for(int y = 0; y < height - 1; y += 2){
                 for(int x = 0; x < width - 1; x += 2){
                    tempXindex++;
                     if(y == 0){
                         tempNodes[tempXindex][tempYindex] = new QuadTreeNode(nodes[x][y], nodes[x+1][y], nodes[x][y+1], nodes[x+1][y+1], scalar, (x * scalar * multiplier) - 1, (y * scalar * multiplier));
                     }else{
                         tempNodes[tempXindex][tempYindex] = new QuadTreeNode(nodes[x][y], nodes[x+1][y], nodes[x][y+1], nodes[x+1][y+1], scalar, (x * scalar * multiplier) - 1, (y * scalar * multiplier) - 1);
                     }
                     if(tempNodes[tempXindex][tempYindex].checkIdenticalBranches()){
                         tempNodes[tempXindex][tempYindex].setTile(nodes[x][y].getTile());
                     }else{
                        tempNodes[tempXindex][tempYindex].setTile(nullTile);
                     }
                 }
                 tempYindex++;
                 tempXindex = 0;
             }
             scalar++;
             return generateQuadTree(tempNodes, width / 2, height / 2, scalar);
         }
     }
 
     public void createMapFromImage(String path){
         try{
             BufferedImage inputMap = ImageIO.read(new File(path));
             for(int y = 0; y < mapHeight; y++){
                 for(int x = 0; x < mapWidth; x++){
                     Color mapColor = new Color(inputMap.getRGB(x, y));
                     int r = mapColor.getRed();
                     int g = mapColor.getGreen();
                     int b = mapColor.getBlue();
                     if(g == 150 && r == 100){
                         mapTiles[x][y] = new Tile(false, true, "C:/Users/Eric/Desktop/AdvanceWarsClone/Sprites/mountainSprite.png", TileTypes.Mountains);
                     }else if(g == 150){
                         mapTiles[x][y] = new Tile(true, false, "C:/Users/Eric/Desktop/AdvanceWarsClone/Sprites/grassSprite.png", TileTypes.Grass);
                     }
                 }
             }
         }catch(IOException e){
             e.printStackTrace();
         }
     }
 
     public void quadTreeRender(Graphics2D g2d, QuadTreeNode branch, int topLeftX, int topLeftY){
         AffineTransform transform;
         AffineTransform scaleTransform = new AffineTransform();
         if(branch.checkIdenticalBranches()){
             transform = g2d.getTransform();
             scaleTransform.scale(branch.getRenderingScalar(), branch.getRenderingScalar());
             g2d.transform(scaleTransform);
             g2d.drawImage(branch.getTile().getTerrainSprite(), null, branch.getX(), branch.getY());
             g2d.setTransform(transform);
         }else{
             QuadTreeNode[] branches = branch.getBranches();
             for(int i = 0; i < 4; i++){
                 if(branches[i].getX() > (topLeftX * branches[i].getRenderingScalar()) && branches[i].getX() < ((topLeftX * 40) * branches[i].getRenderingScalar())){
                     if(branches[i].getY() > (topLeftY * branches[i].getRenderingScalar()) && branches[i].getY() < ((topLeftY * 20) * branches[i].getRenderingScalar())){
                         quadTreeRender(g2d, branches[i], topLeftX, topLeftY);
                     }
                 }
             }
         }
     }
 
     public void render(Graphics2D g2d, int topLeftX, int topLeftY){
         for(int y = topLeftY; y < (topLeftY + 20); y++){
             for(int x = topLeftX; x < (topLeftX + 40); x++){
                 mapTiles[x][y].render(g2d, (x - topLeftX) * multiplier, (y - topLeftY) * multiplier);
             }
         }
     }
 
     public QuadTreeNode[][] getMapTreeRoots(){
         return mapTreeRoots;
     }
 }
