 package de.tum.in.cindy3dplugin.jogl.primitives;
 
 import org.apache.commons.math.geometry.Vector3D;
 
 import de.tum.in.cindy3dplugin.Cindy3DViewer.MeshTopology;
 import de.tum.in.cindy3dplugin.Cindy3DViewer.NormalType;
 import de.tum.in.cindy3dplugin.jogl.Material;
 import de.tum.in.cindy3dplugin.jogl.Util;
 
 /**
  * Grid-based mesh primitive.
  */
 public class Mesh extends Primitive {
 	/**
 	 * Number of existing meshes used to generate unique identifiers.
 	 */
 	private static int meshCounter = 0;
 	/**
 	 * Number of grid rows
 	 */
 	private int rowCount;
 	/**
 	 * Number of column rows
 	 */
 	private int columnCount;
 	/**
 	 * Index of first column which should not be connected to the next. Either
 	 * {@link columnCount} or one less, depending on the {@link topology}.
 	 */
 	private int columnMax;
 	/**
 	 * Index of first row which should not be connected to the next. Either
 	 * {@link rowCount} or one less, depending on the {@link topology}.
 	 */
 	private int rowMax;
 
 	/**
 	 * Number of faces (grid cells)
 	 */
 	private int faceCount;
 
 	/**
 	 * Vertex positions
 	 */
 	private double[][] positions;
 	/**
 	 * Vertex or face normals
 	 */
 	private double[][] normals;
 
 	/**
 	 * Normal type (vertex or face normals)
 	 */
 	private NormalType normalType;
 
 	/**
 	 * Unique identifier
 	 */
 	private int identifier;
 	/**
 	 * Mesh topology
 	 */
 	private MeshTopology topology;
 
 	/**
 	 * Constructs a new grid-based mesh.
 	 * 
 	 * @param rowCount
 	 *            number of grid rows
 	 * @param columnCount
 	 *            number of grid columns
 	 * @param positions
 	 *            vertex positions
 	 * @param normals
 	 *            vertex normals
 	 * @param topology
 	 *            mesh topology
 	 * @param material
 	 *            material
 	 */
 	public Mesh(int rowCount, int columnCount, double[][] positions,
 			double[][] normals, MeshTopology topology, Material material) {
 		super(material);
 		init(rowCount, columnCount, positions, normals, NormalType.PER_VERTEX,
 				topology);
 	}
 
 	/**
 	 * Constructs a new grid-based mesh with auto-generated normals.
 	 * 
 	 * @param rowCount
 	 *            number of grid rows
 	 * @param columnCount
 	 *            number of grid columns
 	 * @param positions
 	 *            vertex positions
 	 * @param normalType
 	 *            type of normals to generate
 	 * @param topology
 	 *            mesh topology
 	 * @param material
 	 *            material
 	 */
 	public Mesh(int rowCount, int columnCount, double[][] positions,
 			NormalType normalType, MeshTopology topology, Material material) {
 		super(material);
 		init(rowCount, columnCount, positions, null, normalType, topology);
 		computeNormals();
 	}
 
 	/**
 	 * Initializes the instance by calculating derived attributes.
 	 * 
 	 * @param rowCount
 	 *            number of grid rows
 	 * @param columnCount
 	 *            number of grid columns
 	 * @param positions
 	 *            vertex positions
 	 * @param normals
 	 *            vertex normals, maybe null
 	 * @param normalType
 	 *            type of normals to generate
 	 * @param topology
 	 *            mesh topology
 	 */
 	private void init(int rowCount, int columnCount, double[][] positions,
 			double[][] normals, NormalType normalType, MeshTopology topology) {
 		this.rowCount = rowCount;
 		this.columnCount = columnCount;
 		this.positions = positions;
 		this.normals = normals;
 		this.normalType = normalType;
 		this.topology = topology;
 		this.identifier = meshCounter++;
 
 		switch (this.topology) {
 		case OPEN:
 			columnMax = columnCount - 1;
 			rowMax = rowCount - 1;
 			break;
 		case CLOSE_ROWS:
 			columnMax = columnCount;
 			rowMax = rowCount - 1;
 			break;
 		case CLOSE_COLUMNS:
 			columnMax = columnCount - 1;
 			rowMax = rowCount;
 			break;
 		case CLOSE_BOTH:
 			columnMax = columnCount;
 			rowMax = rowCount;
 			break;
 		default:
 			return;
 		}
 
 		faceCount = columnMax * rowMax * 2;
 	}
 
 	/**
 	 * @return vertex positions
 	 */
 	public double[][] getPositions() {
 		return positions;
 	}
 
 	/**
 	 * @return vertex or face normals
 	 * @see #getNormalType()
 	 */
 	public double[][] getNormals() {
 		return normals;
 	}
 
 	/**
 	 * @return type of the normals returned by {@link #getNormals()}
 	 */
 	public NormalType getNormalType() {
 		return normalType;
 	}
 
 	/**
 	 * @return unique identifier
 	 */
 	public int getIdentifier() {
 		return identifier;
 	}
 
 	/**
 	 * @return number of faces
 	 */
 	public int getFaceCount() {
 		return faceCount;
 	}
 
 	/**
 	 * @return index first column which should not be connected to the next due
 	 *         to topology
 	 */
 	public int getColumnMax() {
 		return columnMax;
 	}
 
 	/**
 	 * @return index first row which should not be connected to the next due to
 	 *         topology
 	 */
 	public int getRowMax() {
 		return rowMax;
 	}
 
 	/**
 	 * @return number of grid columns
 	 */
 	public int getColumnCount() {
 		return columnCount;
 	}
 
 	/**
 	 * @return number of grid rows
 	 */
 	public int getRowCount() {
 		return rowCount;
 	}
 
 	/**
 	 * Gets the index of the vertex on a given grid position
 	 * 
 	 * @param row
 	 *            vertex grid row
 	 * @param column
 	 *            vertex grid row
 	 * @return index of vertex at the position
 	 */
 	public int getVertexIndex(int row, int column) {
 		return row * columnCount + column;
 	}
 
 	/**
 	 * Computes normals for the mesh.
 	 */
 	private void computeNormals() {
 		normals = null;
 
 		Vector3D[] faceNormals = new Vector3D[faceCount];
 		Vector3D[] vertexNormals = null;
 
 		if (normalType == NormalType.PER_VERTEX) {
 			vertexNormals = new Vector3D[columnCount * rowCount];
 			for (int i = 0; i < vertexNormals.length; ++i) {
 				vertexNormals[i] = Vector3D.ZERO;
 			}
 		}
 
 		Vector3D[] positions = new Vector3D[columnCount * rowCount];
 		for (int i = 0; i < positions.length; ++i) {
 			positions[i] = new Vector3D(this.positions[i][0],
 					this.positions[i][1], this.positions[i][2]);
 		}
 
 		// Iterate over all grid cells (each of which consists of two faces)
 		int faceIndex = 0;
 		for (int row = 0; row < rowMax; ++row) {
 			for (int column = 0; column < columnMax; ++column) {
 				/*
				 *    v1----v2
				 *    |    / |
				 *    |f1 /  |
				 *    |  / f2|
				 *    | /    |
				 *    v3----v4
 				 */
 				int rowPlus1 = (row + 1) % rowCount;
 				int columnPlus1 = (column + 1) % columnCount;
 
 				int v1Index = getVertexIndex(row, column);
 				int v2Index = getVertexIndex(row, columnPlus1);
 				int v3Index = getVertexIndex(rowPlus1, column);
 				int v4Index = getVertexIndex(rowPlus1, columnPlus1);
 
 				Vector3D v1 = positions[v1Index];
 				Vector3D v2 = positions[v2Index];
 				Vector3D v3 = positions[v3Index];
 				Vector3D v4 = positions[v4Index];
 
 				int f1Index = faceIndex++;
 				int f2Index = faceIndex++;
 
 				faceNormals[f1Index] = Vector3D.crossProduct(v2.subtract(v1),
 						v3.subtract(v1)).normalize();
 				faceNormals[f2Index] = Vector3D.crossProduct(v3.subtract(v4),
 						v2.subtract(v4)).normalize();
 
 				if (normalType == NormalType.PER_VERTEX) {
 					vertexNormals[v1Index] = vertexNormals[v1Index]
 							.add(faceNormals[f1Index]);
 					vertexNormals[v2Index] = vertexNormals[v2Index].add(
 							faceNormals[f1Index]).add(faceNormals[f2Index]);
 					vertexNormals[v3Index] = vertexNormals[v3Index].add(
 							faceNormals[f1Index]).add(faceNormals[f2Index]);
 					vertexNormals[v4Index] = vertexNormals[v4Index]
 							.add(faceNormals[f2Index]);
 				}
 			}
 		}
 
 		if (normalType == NormalType.PER_VERTEX) {
 			normals = new double[columnCount * rowCount][3];
 			for (int i = 0; i < vertexNormals.length; ++i) {
 				normals[i] = Util.vectorToDoubleArray(vertexNormals[i]
 						.normalize());
 			}
 		} else if (normalType == NormalType.PER_FACE) {
 			normals = new double[faceCount][3];
 			for (int i = 0; i < normals.length; ++i) {
 				normals[i] = Util.vectorToDoubleArray(faceNormals[i]);
 			}
 		}
 	}
 }
