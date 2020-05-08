 package graphics;
 
 import java.io.*;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 /*
  *  lass to load Wavefront .obj files
  */
 public class WavefrontObjLoader {
     public static void main(String[] args) {
         File file = new File("assets/cube.obj");
         load(file);
         System.out.println("Complete");
     }
 
     public static Model load(String filepath){
         return load(new File(filepath));
     }
 
     public static Model load(File file) {
         WavefrontObjLoader wol = new WavefrontObjLoader();
         BufferedReader in;
         String line;
 
         try {
             in = new BufferedReader(new FileReader(file));
 
             // While we have lines in our file
             while((line = in.readLine()) != null)
                 wol.readLine(line);
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
 
         return wol.generateModel();
     }
 
     private Vector<ObjVertex> geo_verticies;
     private Vector<ObjVertex> texture_verticies;
     private Vector<ObjVertex> vertex_normals;
     private Vector<Polygon> polygons;
 
 
     private WavefrontObjLoader() {
         geo_verticies = new Vector<ObjVertex>();
         texture_verticies = new Vector<ObjVertex>();
         vertex_normals = new Vector<ObjVertex>();
         polygons = new Vector<Polygon>();
     }
 
     private void readLine(String line) {
         StringTokenizer tokenizer = new StringTokenizer(line);
 
         // And while we have tokens in the line
         while(tokenizer.hasMoreTokens()) {
             String token = tokenizer.nextToken();
 
             switch(tokenType(token)) {
                 case COMMENT:
                     /* NOOP - skip remainder of this line */
                     return;
                 case GROUP:
                     break;
                 case GEOMETRIC_VERTEX:
                     geo_verticies.add(readVertex(tokenizer));
                     break;
                 case VERTEX_NORMAL:
                     vertex_normals.add(readVertex(tokenizer));
                     break;
                 case TEXTURE_COORDINATE:
                     texture_verticies.add(readVertex(tokenizer));
                     break;
                 case USEMAP:
                     break;
                 case MAPLIB:
                     break;
                 case MATLIB:
                     // Do we need to handle relative paths here?
                     while (tokenizer.hasMoreTokens())
                         WavefrontMtlLoader.load(tokenizer.nextToken());
                     break;
                 case USEMTL:
                     break;
                 case FACE:
                     polygons.add(readPolygon(tokenizer));
                     break;
                 default:
                     System.out.println("Unhandled Token: " + token + "\n" +"Line: " + line);
                     return;
             }
         }
     }
 
     private TokenType tokenType(String token) {
         if (token.equals("#"))
             return TokenType.COMMENT;
         if (token.equals("v"))
             return TokenType.GEOMETRIC_VERTEX;
         if (token.equals("vt"))
             return TokenType.TEXTURE_COORDINATE;
         if (token.equals("vn"))
             return TokenType.VERTEX_NORMAL;
         if (token.equals("f"))
             return TokenType.FACE;
         if (token.equals("usemap"))
             return TokenType.USEMAP;
         if (token.equals("usemtl"))
             return TokenType.USEMTL;
         if (token.equals("maplib"))
             return TokenType.MAPLIB;
         if (token.equals("mtllib"))
             return TokenType.MATLIB;
         if (token.equals("g"))
             return TokenType.GROUP;
        if (token.regionMatches(0, "#", 0, 1))
            return TokenType.COMMENT;
 
         return TokenType.UNKNOWN;
     }
 
     private ObjVertex readVertex(StringTokenizer tokenizer) {
         ObjVertex vertex = new ObjVertex();
         float value;
 
         while (tokenizer.hasMoreTokens()) {
             value = Float.parseFloat(tokenizer.nextToken());
             vertex.add(value);
         }
         return vertex;
     }
 
     private Polygon readPolygon(StringTokenizer tokenizer) {
         Vector<Vertex> verticies = new Vector<Vertex>();
         while (tokenizer.hasMoreTokens()) {
             String token = tokenizer.nextToken();
             String vertex_tokens[] = token.split("/");
             @SuppressWarnings("unused")
             ObjVertex geo, tex, norm;
 
             switch(vertex_tokens.length) {
                 case 1:
                     /* Single number - lookup geo vertex only */
                     geo = geo_verticies.get(Integer.parseInt(vertex_tokens[0]) - 1); /* Obj files starts w/ vertex 1 */
                     verticies.add(new Vertex(geo.x, geo.y, geo.z));
                     break;
                 case 2:
                     /* Two numbers - geo vertex, and texture coordinate */
                     geo = geo_verticies.get(Integer.parseInt(vertex_tokens[0]) - 1);
                     tex = texture_verticies.get(Integer.parseInt(vertex_tokens[1]) - 1);
                     verticies.add(new Vertex(geo.x, geo.y, geo.z, tex.x, tex.y));
                     break;
                 case 3:
                     /* geo vertex, texture coordinate and normal */
                     geo = geo_verticies.get(Integer.parseInt(vertex_tokens[0]) - 1);
                     tex = texture_verticies.get(Integer.parseInt(vertex_tokens[1]) - 1);
                     norm = vertex_normals.get(Integer.parseInt(vertex_tokens[2]) - 1);
                     verticies.add(new Vertex(geo.x, geo.y, geo.z, tex.x, tex.y));
                     break;
                 default:
                     throw new IllegalArgumentException("Malformed vertex token: " + token);
             }
         }
 
         return new Polygon("NONE" /* materialName */, verticies);
     }
 
     private Model generateModel() {
         return new Model(polygons);
     }
 
     private static enum TokenType {
         UNKNOWN,
         COMMENT,
         GEOMETRIC_VERTEX,
         VERTEX_NORMAL,
         TEXTURE_COORDINATE,
         FACE,
         USEMAP,
         USEMTL,
         MAPLIB,
         MATLIB,
         GROUP,
     }
 
     private static class ObjVertex {
         int dim;
         @SuppressWarnings("unused")
         float x, y, z, w;
 
         public ObjVertex() {
             dim = 0;
             this.x = 0.0f;
             this.y = 0.0f;
             this.z = 0.0f;
             this.w = 0.0f;
         }
 
         void add(float v) {
             switch(dim) {
                 case 0:
                     x = v;
                     dim ++;
                     return;
                 case 1:
                     y = v;
                     dim ++;
                     return;
                 case 2:
                     z = v;
                     dim ++;
                     return;
                 case 3:
                     w = v;
                     dim ++;
                     return;
                 default:
                     throw new IllegalArgumentException();
             }
         }
     }
 }
