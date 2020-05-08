 package de.widemeadows.projectcore.math;
 
 import de.widemeadows.projectcore.cache.annotations.ReturnsCachedValue;
 import de.widemeadows.projectcore.math.exceptions.MatrixException;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import de.widemeadows.projectcore.cache.ObjectCache;
 import de.widemeadows.projectcore.cache.ObjectFactory;
 
 /**
  * 4D-Matrix.
  * <p>
  *     Neue Objekte werden erzeugt mittels {@link #createNew()}; Nicht länger benötigte Objekte sollten per
  *     {@link #recycle(Matrix4)} zurückgegeben werden.
  * </p>
  */
 public final class Matrix4 {
 
 	/**
 	 * Instanz, die die Verwaltung nicht länger benötigter Instanzen übernimmt
 	 */
 	public static final ObjectCache<Matrix4> Recycling = new ObjectCache<Matrix4>(new ObjectFactory<Matrix4>() {
 		@NotNull
         @Override
 		public Matrix4 createNew() {
 			return new Matrix4();
 		}
 	});
 
 	/**
 	 * Erzeugt eine neue Matrix-Instanz.
 	 * <p>
 	 *     <strong>Hinweis:</strong> Der Zustand der Matrix kann korrupt sein!
 	 * </p>
 	 * @return Die neue oder aufbereitete Matrix
 	 * @see #Recycling
 	 */
 	@NotNull
 	public static Matrix4 createNew() {
         return Recycling.getOrCreate().toUnit();
 	}
 
 	/**
 	 * Registriert eine Matrix für das spätere Recycling
 	 * 
 	 * @param matrix Die zu registrierende Matrix
 	 * @see #Recycling
      * @see Matrix4#recycle()
      */
 	public static void recycle(@NotNull Matrix4 matrix) {
 		Recycling.registerElement(matrix);
 	}
 
     /**
      * Registriert diese Matrix für das spätere Recycling
      *
      * @see #Recycling
      * @see Matrix4#recycle(Matrix4)
      */
     public void recycle() {
         Recycling.registerElement(this);
     }
 	
 	/**
 	 * Die Elemente
 	 */
 	@NotNull
     public final float[] values = new float[16];
 	
 	/**
 	 * Die Einheitsmatrix
 	 */
 	@NotNull
 	public static final Matrix4 UNIT = new Matrix4(
 		1.0f, 0.0f, 0.0f, 0.0f,
 		0.0f, 1.0f, 0.0f, 0.0f,
 		0.0f, 0.0f, 1.0f, 0.0f,
 		0.0f, 0.0f, 0.0f, 1.0f);
 
 	/**  Zeile 1, Spalte 1 */
 	public static final int M11 = 0;
 	/**  Zeile 1, Spalte 2 */
 	public static final int M12 = 1;
 	/**  Zeile 1, Spalte 3 */
 	public static final int M13 = 2;
 	/**  Zeile 1, Spalte 4 */
 	public static final int M14 = 3;
 
 	/**  Zeile 2, Spalte 1 */
 	public static final int M21 = 4;
 	/**  Zeile 2, Spalte 2 */
 	public static final int M22 = 5;
 	/**  Zeile 2, Spalte 3 */
 	public static final int M23 = 6;
 	/**  Zeile 2, Spalte 4 */
 	public static final int M24 = 7;
 
 	/**  Zeile 3, Spalte 1 */
 	public static final int M31 = 8;
 	/**  Zeile 3, Spalte 2 */
 	public static final int M32 = 9;
 	/**  Zeile 3, Spalte 3 */
 	public static final int M33 = 10;
 	/**  Zeile 3, Spalte 4 */
 	public static final int M34 = 11;
 
 	/**  Zeile 4, Spalte 1 */
 	public static final int M41 = 12;
 	/**  Zeile 4, Spalte 2 */
 	public static final int M42 = 13;
 	/**  Zeile 4, Spalte 3 */
 	public static final int M43 = 14;
 	/**  Zeile 4, Spalte 4 */
 	public static final int M44 = 15;
 
 	/**
 	 * Erzeugt eine neue, leere Matrix
 	 */
 	private Matrix4() {
 	}
 
 	/**
 	 * Errzeugt eine neue Matrix aus ihren Komponenten
 	 * 
 	 * @param m11 Zeile 1, Spalte 1
 	 * @param m12 Zeile 1, Spalte 2
 	 * @param m13 Zeile 1, Spalte 3
 	 * @param m14 Zeile 1, Spalte 4
 	 * @param m21 Zeile 2, Spalte 1
 	 * @param m22 Zeile 2, Spalte 2
 	 * @param m23 Zeile 2, Spalte 3
 	 * @param m24 Zeile 2, Spalte 4
 	 * @param m31 Zeile 3, Spalte 1
 	 * @param m32 Zeile 3, Spalte 2
 	 * @param m33 Zeile 3, Spalte 3
 	 * @param m34 Zeile 3, Spalte 4
 	 * @param m41 Zeile 4, Spalte 1
 	 * @param m42 Zeile 4, Spalte 2
 	 * @param m43 Zeile 4, Spalte 3
 	 * @param m44 Zeile 4, Spalte 4
 	 */
 	public Matrix4(	float m11, float m12, float m13, float m14,
 					float m21, float m22, float m23, float m24,
 					float m31, float m32, float m33, float m34,
 					float m41, float m42, float m43, float m44
 	) {
 		// Array füllen
 		values[ 0] = m11;	values[ 1] = m12;	values[ 2] = m13;	values[ 3] = m14;
 		values[ 4] = m21;	values[ 5] = m22;	values[ 6] = m23;	values[ 7] = m24;
 		values[ 8] = m31;	values[ 9] = m32;	values[10] = m33;	values[11] = m34;
 		values[12] = m41;	values[13] = m42;	values[14] = m43;	values[15] = m44;
 	}
 
 	/**
 	 * Bezieht das Element am angegebenen Feldindex
 	 *
 	 * @param index Der Feldindex (0..14)
 	 * @return Der Wert
 	 */
 	public float getAt(int index) {
 		assert index >= 0 && index <= 15;
 		return values[index];
 	}
 
 	/**
 	 * Bezieht den Wert an der angegebenen Zeile und SPalte
 	 *
 	 * @param row Die Zeile (nullbasiert!)
 	 * @param column Die Spalte (nullbasiert!)
 	 * @return Der Wert
 	 */
 	public float getAt(int row, int column) {
 		assert row >= 0 && row < 4 && column >= 0 && column < 4;
 		return values[row * 4 + column];
 	}
 
 	/**
 	 * Setzt den Wert an der angegeben Position
 	 *
 	 * @param index Der Feldindex (0..15)
 	 * @param value Der Wert
 	 */
 	public void setAt(int index, float value) {
 		assert index >= 0 && index <= 15;
 		values[index] = value;
 	}
 
 	/**
 	 * Setzt den Wert an der angegeben Zeile und Spalte
 	 *
 	 * @param row Die Zeile (nullbasiert!)
 	 * @param column Die Spalte (nullbasiert!)
 	 * @param value Der Wert
 	 */
 	public void setAt(int row, int column, float value) {
 		assert row >= 0 && row < 4 && column >= 0 && column < 4;
 		values[row * 4 + column] = value;
 	}
 
 	/**
 	 * Setzt die Matrix aus ihren Einzelkomponenten
 	 *
 	 * @param m11 Zeile 1, Spalte 1
 	 * @param m12 Zeile 1, Spalte 2
 	 * @param m13 Zeile 1, Spalte 3
 	 * @param m14 Zeile 1, Spalte 4
 	 * @param m21 Zeile 2, Spalte 1
 	 * @param m22 Zeile 2, Spalte 2
 	 * @param m23 Zeile 2, Spalte 3
 	 * @param m24 Zeile 2, Spalte 4
 	 * @param m31 Zeile 3, Spalte 1
 	 * @param m32 Zeile 3, Spalte 2
 	 * @param m33 Zeile 3, Spalte 3
 	 * @param m34 Zeile 3, Spalte 4
 	 * @param m41 Zeile 4, Spalte 1
 	 * @param m42 Zeile 4, Spalte 2
 	 * @param m43 Zeile 4, Spalte 3
 	 * @param m44 Zeile 4, Spalte 4
 	 *
 	 * @return Dieselbe Instanz für method chaining
 	 */
 	public Matrix4 set(float m11, float m12, float m13, float m14,
 					float m21, float m22, float m23, float m24,
 					float m31, float m32, float m33, float m34,
 					float m41, float m42, float m43, float m44
 	) {
 		values[ 0] = m11;	values[ 1] = m12;	values[ 2] = m13;	values[ 3] = m14;
 		values[ 4] = m21;	values[ 5] = m22;	values[ 6] = m23;	values[ 7] = m24;
 		values[ 8] = m31;	values[ 9] = m32;	values[10] = m33;	values[11] = m34;
 		values[12] = m41;	values[13] = m42;	values[14] = m43;	values[15] = m44;
 
 		return this;
 	}
 
 	/**
 	 * Wandelt die Matrix in die Einheitsmatrix um
      * @return Dieselbe Instanz für method chaining
 	 */
 	public Matrix4 toUnit() {
 		values[ 0] = 1.0f;	values[ 1] = 0.0f;	values[ 2] = 0.0f;	values[ 3] = 0.0f;
 		values[ 4] = 0.0f;	values[ 5] = 1.0f;	values[ 6] = 0.0f;	values[ 7] = 0.0f;
 		values[ 8] = 0.0f;	values[ 9] = 0.0f;	values[10] = 1.0f;	values[11] = 0.0f;
 		values[12] = 0.0f;	values[13] = 0.0f;	values[14] = 0.0f;	values[15] = 1.0f;
         return this;
 	}
 
 	/**
 	 * Multipliziert die Matrix mit einem Faktor und liefert das Ergebnis
 	 *
 	 * @param f Der Faktor
 	 * @return Das Ergebnis (Kopie!)
 	 * @see Matrix4#mulInPlace(float) 
 	 */
 	@NotNull
 	@ReturnsCachedValue
 	public Matrix4 mul(final float f) {
 		return createNew().set(f * values[0], f * values[1], f * values[2], f * values[3], f * values[4], f * values[5], f * values[6], f * values[7], f * values[8], f * values[9], f * values[10], f * values[11], f * values[12], f * values[13], f * values[14], f * values[15]);
 	}
 
 	/**
 	 * Multipliziert die Matrix mit einem Faktor
 	 *
 	 * @param f Der Faktor
 	 * @see Matrix4#mul(float) 
 	 */
 	@NotNull
 	public Matrix4 mulInPlace(float f) {
 		values[ 0] *= f;	values[ 1] *= f;	values[ 2] *= f;	values[ 3] *= f;
 		values[ 4] *= f;	values[ 5] *= f;	values[ 6] *= f;	values[ 7] *= f;
 		values[ 8] *= f;	values[ 9] *= f;	values[10] *= f;	values[11] *= f;
 		values[12] *= f;	values[13] *= f;	values[14] *= f;	values[15] *= f;
 		return this;
 	}
 	
 	/**
 	 * Multipliziert eine Matrix mit einer zweiten
 	 *
 	 * @param b Die zweite Matrix
 	 *
 	 * @return Das Ergebnis
 	 */
 	@NotNull
 	@ReturnsCachedValue
 	public Matrix4 mul(@NotNull final Matrix4 b) {
 		return createNew().set(
 
 				values[M11] * b.values[M11] + values[M12] * b.values[M21] + values[M13] * b.values[M31] + values[M14] * b.values[M41],
 				values[M11] * b.values[M12] + values[M12] * b.values[M22] + values[M13] * b.values[M32] + values[M14] * b.values[M42],
 				values[M11] * b.values[M13] + values[M12] * b.values[M23] + values[M13] * b.values[M33] + values[M14] * b.values[M43],
 				values[M11] * b.values[M14] + values[M12] * b.values[M24] + values[M13] * b.values[M34] + values[M14] * b.values[M44],
 
 				values[M21] * b.values[M11] + values[M22] * b.values[M21] + values[M23] * b.values[M31] + values[M24] * b.values[M41],
 				values[M21] * b.values[M12] + values[M22] * b.values[M22] + values[M23] * b.values[M32] + values[M24] * b.values[M42],
 				values[M21] * b.values[M13] + values[M22] * b.values[M23] + values[M23] * b.values[M33] + values[M24] * b.values[M43],
 				values[M21] * b.values[M14] + values[M22] * b.values[M24] + values[M23] * b.values[M34] + values[M24] * b.values[M44],
 
 				values[M31] * b.values[M11] + values[M32] * b.values[M21] + values[M33] * b.values[M31] + values[M34] * b.values[M41],
 				values[M31] * b.values[M12] + values[M32] * b.values[M22] + values[M33] * b.values[M32] + values[M34] * b.values[M42],
 				values[M31] * b.values[M13] + values[M32] * b.values[M23] + values[M33] * b.values[M33] + values[M34] * b.values[M43],
 				values[M31] * b.values[M14] + values[M32] * b.values[M24] + values[M33] * b.values[M34] + values[M34] * b.values[M44],
 
 				values[M41] * b.values[M11] + values[M42] * b.values[M21] + values[M43] * b.values[M31] + values[M44] * b.values[M41],
 				values[M41] * b.values[M12] + values[M42] * b.values[M22] + values[M43] * b.values[M32] + values[M44] * b.values[M42],
 				values[M41] * b.values[M13] + values[M42] * b.values[M23] + values[M43] * b.values[M33] + values[M44] * b.values[M43],
 				values[M41] * b.values[M14] + values[M42] * b.values[M24] + values[M43] * b.values[M34] + values[M44] * b.values[M44]);
 	}
 
 	/**
 	 * Erzeugt eine Kopie dieser Matrix
 	 *
 	 * @return Die Kopie
 	 */
 	@NotNull
 	@ReturnsCachedValue
 	public Matrix4 clone() {
 		return createNew().set(values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15]);
 	}
 	
 	/**
 	 * Bezieht die Determinante der Matrix
 	 *
 	 * @return Die Determinante
 	 *
 	 * @see Matrix4#getSubDeterminant(int, int)
 	 */
 	public float getDeterminant() {
 		/*
 		return
 			Cell[0, 0]*GetSubDeterminant(0, 0) -
 			Cell[0, 1]*GetSubDeterminant(0, 1) +
 			Cell[0, 2]*GetSubDeterminant(0, 2) -
 			Cell[0, 3]*GetSubDeterminant(0, 3);
 		*/
 
 		//double value1 = Cell[0, 0]*(Cell[1, 1]*Cell[2, 2]*Cell[3, 3] +
 		//                            Cell[1, 2]*Cell[2, 3]*Cell[3, 1] +
 		//                            Cell[1, 3]*Cell[2, 1]*Cell[3, 2] -
 		//                            Cell[1, 3]*Cell[2, 2]*Cell[3, 1] -
 		//                            Cell[1, 1]*Cell[2, 3]*Cell[3, 2] -
 		//                            Cell[1, 2]*Cell[2, 1]*Cell[3, 3]);
 
 		//double value2 = Cell[0, 1]*(Cell[1, 0]*Cell[2, 2]*Cell[3, 3] +
 		//                            Cell[1, 2]*Cell[2, 3]*Cell[3, 0] +
 		//                            Cell[1, 3]*Cell[2, 0]*Cell[3, 2] -
 		//                            Cell[1, 3]*Cell[2, 2]*Cell[3, 0] -
 		//                            Cell[1, 0]*Cell[2, 3]*Cell[3, 2] -
 		//                            Cell[1, 2]*Cell[2, 0]*Cell[3, 3]);
 
 		//double value3 = Cell[0, 2]*(Cell[1, 0]*Cell[2, 1]*Cell[3, 3] +
 		//                            Cell[1, 1]*Cell[2, 3]*Cell[3, 0] +
 		//                            Cell[1, 3]*Cell[2, 0]*Cell[3, 1] -
 		//                            Cell[1, 3]*Cell[2, 1]*Cell[3, 0] -
 		//                            Cell[1, 0]*Cell[2, 3]*Cell[3, 1] -
 		//                            Cell[1, 1]*Cell[2, 0]*Cell[3, 3]);
 
 		//double value4 = Cell[0, 3]*(Cell[1, 0]*Cell[2, 1]*Cell[3, 2] +
 		//                            Cell[1, 1]*Cell[2, 2]*Cell[3, 0] +
 		//                            Cell[1, 2]*Cell[2, 0]*Cell[3, 1] -
 		//                            Cell[1, 2]*Cell[2, 1]*Cell[3, 0] -
 		//                            Cell[1, 0]*Cell[2, 2]*Cell[3, 1] -
 		//                            Cell[1, 1]*Cell[2, 0]*Cell[3, 2]);
 
 		float c1122 = getAt(1, 1)*getAt(2, 2);
 		float c1223 = getAt(1, 2)*getAt(2, 3);
 		float c1233 = getAt(1, 2)*getAt(3, 3);
 		float c1332 = getAt(1, 3)*getAt(3, 2);
 		float c1322 = getAt(1, 3)*getAt(2, 2);
 		float c1123 = getAt(1, 1)*getAt(2, 3);
 		float c1021 = getAt(1, 0)*getAt(2, 1);
 		float c1022 = getAt(1, 0)*getAt(2, 2);
 		float c1023 = getAt(1, 0)*getAt(2, 3);
 		float c1120 = getAt(1, 1)*getAt(2, 0);
 		float c2031 = getAt(2, 0)*getAt(3, 1);
 		float c2130 = getAt(2, 1)*getAt(3, 0);
 
 		float value1 = getAt(0, 0)*(c1122*getAt(3, 3) +
 		                            c1223*getAt(3, 1) +
 		                            c1332*getAt(2, 1) -
 		                            c1322*getAt(3, 1) -
 		                            c1123*getAt(3, 2) -
 		                            c1233*getAt(2, 1));
 
 		float value2 = getAt(0, 1)*(c1022*getAt(3, 3) +
 		                            c1223*getAt(3, 0) +
 		                            c1332*getAt(2, 0) -
 		                            c1322*getAt(3, 0) -
 		                            c1023*getAt(3, 2) -
 		                            c1233*getAt(2, 0));
 
 		float value3 = getAt(0, 2)*(c1021*getAt(3, 3) +
 		                            c1123*getAt(3, 0) +
                                     c2031*getAt(1, 3) -
                                     c2130*getAt(1, 3) -
 		                            c1023*getAt(3, 1) -
 		                            c1120*getAt(3, 3));
 
 		float value4 = getAt(0, 3)*(c1021*getAt(3, 2) +
 		                            c1122*getAt(3, 0) +
                                     c2031*getAt(1, 2) -
                                     c2130*getAt(1, 2) -
 		                            c1022*getAt(3, 1) -
 		                            c1120*getAt(3, 2));
 
 		return value1 - value2 + value3 - value4;
 	}
 	
 	/**
 	 * Bezieht die 3x3 Unterdeterminante der Matrix durch Ignorieren bestimmter Zeilen und Spalten
 	 *
 	 * @param row Die zu überspringende Zeile
 	 * @param column Die zu überspringende Spalte
 	 * @return Die Unterdeterminante
 	 *
 	 * @see Matrix4#getDeterminant()
 	 */
 	public float getSubDeterminant(int row, int column) {
 		assert row >= 0 && row < 4;
 		assert column >= 0 && column < 4;
 
 		// get target row indices
 		int row0 = 0;
 		int row1 = 1;
 		int row2 = 2;
 
 		// get target column indices
 		int col0 = 0;
 		int col1 = 1;
 		int col2 = 2;
 
 		// adjust for skipped rows
 		if (row == 0) { ++row0; ++row1; ++row2; }
 		else if (row == 1) { ++row1; ++row2; }
 		else if (row == 2) { ++row2; }
 
 		// adjust for skipped columns
 		if (column == 0) { ++col0; ++col1; ++col2; }
 		else if (column == 1) { ++col1; ++col2; }
 		else if (column == 2) { ++col2; }
 
 		float r0c0 = getAt(row0, col0); // TODO: Order by row or column access when it is clear what is needed!
 		float r1c1 = getAt(row1, col1);
 		float r0c1 = getAt(row0, col1);
 		float r1c0 = getAt(row1, col0);
 		float r2c2 = getAt(row2, col2);
 		float r2c1 = getAt(row2, col1);
 		float r0c2 = getAt(row0, col2);
 		float r1c2 = getAt(row1, col2);
 		float r2c0 = getAt(row2, col0);
 		
 		// Regel von Sarrus
 		// gem. Mathematische Formelsammlung, 9. Auflage, Papula, S. 201
 		return 	r0c0 * r1c1 * r2c2 +
 				r0c1 * r1c2 * r2c0 +
 				r0c2 * r1c0 * r2c1 -
 				r0c2 * r1c1 * r2c0 -
 				r0c0 * r1c2 * r2c1 -
 				r0c1 * r1c0 * r2c2;
 	}
 	
 	/**
 	 * Bezieht die Adjunkte
 	 *
 	 * @return Die Adjunkte
 	 */
 	@NotNull
 	@ReturnsCachedValue
 	public Matrix4 getAdjoint() {
 		/*
 		return new Matrix4D(
 			+GetSubDeterminant(0, 0), -GetSubDeterminant(0, 1), +GetSubDeterminant(0, 2), -GetSubDeterminant(0, 3),
 			-GetSubDeterminant(1, 0), +GetSubDeterminant(1, 1), -GetSubDeterminant(1, 2), +GetSubDeterminant(1, 3),
 			+GetSubDeterminant(2, 0), -GetSubDeterminant(2, 1), +GetSubDeterminant(2, 2), -GetSubDeterminant(2, 3),
 			-GetSubDeterminant(3, 0), +GetSubDeterminant(3, 1), -GetSubDeterminant(3, 2), +GetSubDeterminant(3, 3)).GetTransposed();
 		*/
 
         float c00 = getAt(0, 0);
         float c01 = getAt(0, 1);
         float c02 = getAt(0, 2);
         float c03 = getAt(0, 3);
         
         float c10 = getAt(1, 0);
         float c11 = getAt(1, 1);
         float c12 = getAt(1, 2);
         float c13 = getAt(1, 3);
         
         float c20 = getAt(2, 0);
         float c21 = getAt(2, 1);
         float c22 = getAt(2, 2);
         float c23 = getAt(2, 3);
 
         float c30 = getAt(3, 0);
         float c31 = getAt(3, 1);
         float c32 = getAt(3, 2);
         float c33 = getAt(3, 3);
 
         // Erster Block
 		float c1122 = c11*c22;
         float c1223 = c12*c23;
         float c1321 = c13*c21;
         float c1322 = c13*c22;
         float c1123 = c11*c23;
         float c1221 = c12*c21;
 
         // Determinant row 0, cell 0
         float m11 = c1122 * c33 +
                     c1223 * c31 +
                     c1321 * c32 -
                     c1322 * c31 -
                     c1123 * c32 -
                     c1221 * c33;
 
         // Zweiter Block
         float c2233 = c22 * c33;
         float c1320 = c13 * c20;
         float c2332 = c23 * c32;
         float c1220 = c12 * c20;
 
         // Determinant row 0, cell 1
         float m12 = c2233 * c10 +
                     c1223 * c30 +
                     c1320 * c32 -
                     c1322 * c30 -
                     c2332 * c10 -
                     c1220 * c33;
 
         // Dritter Block
         float c2133 = c21 * c33;
         float c2331 = c23 * c31;
         float c2033 = c20 * c33;
 
         // Determinant row 0, cell 2
         float m13 = c2133 * c10 +
                     c1123 * c30 +
                     c1320 * c31 -
                     c1321 * c30 -
                     c2331 * c10 -
                     c2033 * c11;
 
         // Vierter Block
         float c2231 = c22 * c31;
         float c2032 = c20 * c32;
         float c2132 = c21 * c32;
 
 		// Determinant row 0, cell 3
 		float m14 =  c2132*c10 +
 		             c1122*c30 +
 		             c1220*c31 -
 		             c1221*c30 -
                      c2231*c10 -
                      c2032*c11;
 
 		// Determinant row 1, cell 0
 		float m21 =  c2233*c01 +
                      c2331*c02 +
                      c2132*c03 -
                      c2231*c03 -
                      c2332*c01 -
                      c2133*c02;
 
         // Fünfter Block
         float c1233 = c12 * c33;
         float c1133 = c11 * c33;
         float c1332 = c13 * c32;
         float c1231 = c12 * c31;
         float c1132 = c11 * c32;
         float c1331 = c13 * c31;
 
 		// Determinant row 2, cell 0
 		float m31 =  c1233*c01 +
 		             c1331*c02 +
 		             c1132*c03 -
 		             c1231*c03 -
 		             c1332*c01 -
 		             c1133*c02;
 
         // Sechster Block
         float c1032 = c10 * c32;
         float c1330 = c13 * c30;
         float c1230 = c12 * c30;
         float c1033 = c10 * c33;
 
 		// Determinant row 2, cell 1
 		float m32 =  c1233*c00 +
 		             c1330*c02 +
 		             c1032*c03 -
 		             c1230*c03 -
 		             c1332*c00 -
 		             c1033*c02;
 
         // Siebter Block
         float c1130 = c11 * c30;
         float c1031 = c10 * c31;
 
         // Determinant row 2, cell 2
 		float m33 =  c1133*c00 +
 		             c1330*c01 +
 		             c1031*c03 -
 		             c1130*c03 -
 		             c1331*c00 -
 		             c1033*c01;
 
 		// Determinant row 2, cell 3
 		float m34 =  c1132*c00 +
 		             c1230*c01 +
 		             c1031*c02 -
 		             c1130*c02 -
 		             c1231*c00 -
 		             c1032*c01;
 
         // Achter Block
         float c1022 = c10 * c22;
         float c1023 = c10 * c23;
 
         // Determinant row 3, cell 1
         float m42 = c1223 * c00 +
                 c1320 * c02 +
                 c1022 * c03 -
                 c1220 * c03 -
                 c1322 * c00 -
                 c1023 * c02;
 
         // Neunter Block
         float m43, m44;
         {
             float c1021 = c10 * c21;
             float c1120 = c11 * c20;
 
             // Determinant row 3, cell 2
             m43 = c1123 * c00 +
                     c1320 * c01 +
                     c1021 * c03 -
                     c1120 * c03 -
                     c1321 * c00 -
                     c1023 * c01;
 
             // Determinant row 3, cell 3
             m44 = c1122 * c00 +
                     c1220 * c01 +
                     c1021 * c02 -
                     c1120 * c02 -
                     c1221 * c00 -
                     c1022 * c01;
         }
 
         // Zehnter Block
         float c2230 = c22 * c30;
         float c2330 = c23 * c30;
 
         // Determinant row 1, cell 1
         float m22 = c2233 * c00 +
                 c2330 * c02 +
                 c2032 * c03 -
                 c2230 * c03 -
                 c2332 * c00 -
                 c2033 * c02;
 
         // Elfter Block
         float m24, m23;
         {
             float c2031 = c20 * c31;
             float c2130 = c21 * c30;
 
             // Determinant row 1, cell 2
             m23 = c2133 * c00 +
                     c2330 * c01 +
                     c2031 * c03 -
                     c2130 * c03 -
                     c2331 * c00 -
                     c2033 * c01;
 
             // Determinant row 1, cell 3
             m24 = c2132 * c00 +
                     c2230 * c01 +
                     c2031 * c02 -
                     c2130 * c02 -
                     c2231 * c00 -
                     c2032 * c01;
         }
 
         // Determinant row 3, cell 0
         float m41 = c1223 * c01 +
                 c1321 * c02 +
                 c1122 * c03 -
                 c1221 * c03 -
                 c1322 * c01 -
                 c1123 * c02;
 
 		//return new Matrix4D(
 		//    +m11, -m12, +m13, -m14,
 		//    -m21, +m22, -m23, +m24,
 		//    +m31, -m32, +m33, -m34,
 		//    -m41, +m42, -m43, +m44).GetTransposed();
 
 		// Directly transpose the matrix by swapping the field indices
 		return createNew().set(
 			+m11, -m21, +m31, -m41,
 			-m12, +m22, -m32, +m42,
 			+m13, -m23, +m33, -m43,
 			-m14, +m24, -m34, +m44);
 	}
 	
 	/**
 	 * Invertiert die Matrix
 	 *
 	 * @return Die invertierte Matrix
 	 * @throws MatrixException Matrix ist nicht invertierbar
 	 *
 	 * @see Matrix4#getInvertedNoThrow()
 	 */
 	@NotNull
 	public Matrix4 getInverted() throws MatrixException {
 		float invDeterminant = 1.0f/getDeterminant();
 		if (Float.isInfinite(invDeterminant)) throw new MatrixException("Matrix cannot be inverted.");
 		return getAdjoint().mul(invDeterminant);
 	}
 
 	/**
 	 * Invertiert die Matrix
 	 *
 	 * @return Die invertierte Matrix oder <code>null</code>, wenn die Matrix nicht invertierbar ist
 	 *
 	 * @see Matrix4#getInverted()
 	 */
 	@Nullable
 	@ReturnsCachedValue
 	public Matrix4 getInvertedNoThrow() {
 		float invDeterminant = 1.0f / getDeterminant();
 		if (Float.isInfinite(invDeterminant)) return null;
 		return getAdjoint().mulInPlace(invDeterminant);
 	}
 	
 	/**
 	 * Wandelt die Matrix in eine Translationsmatrix um
 	 *<h2>Form der Matrix</h2>
      * <pre>
      * +---------+---+
      * | 1  0  0 | 0 |
      * | 0  1  0 | 0 |
      * | 0  0  1 | 0 |
      * +---------+---+
      * |Tx Ty Tz | 1 |
      * +---------+---+
      * </pre>
      *
 	 * @param translation Der Translationsvektor
 	 *
 	 * @see Matrix4#toTranslation(float, float, float)
 	 */
 	public void toTranslation(@NotNull final Vector3 translation) {
 		values[M11] = 1f; values[M12] = 0f; values[M13] = 0f; values[M14] = 0f;
 		values[M21] = 0f; values[M22] = 1f; values[M23] = 0f; values[M24] = 0f;
 		values[M31] = 0f; values[M32] = 0f; values[M33] = 1f; values[M34] = 0f;
 		values[M41] = translation.x;
 		values[M42] = translation.y;
 		values[M43] = translation.z; 
 		values[M44] = 1f;
 	}
 	
 	/**
 	 * Wandelt die Matrix in eine Translationsmatrix um
      * *
      * <h2>Form der Matrix</h2>
      * <pre>
      * +---------+---+
      * | 1  0  0 | 0 |
      * | 0  1  0 | 0 |
      * | 0  0  1 | 0 |
      * +---------+---+
      * | x  y  z | 1 |
      * +---------+---+
      * </pre>
 	 *
 	 * @param x Die X-Komponente des Translationsvektors
 	 * @param y Die Y-Komponente des Translationsvektors
 	 * @param z Die Z-Komponente des Translationsvektors
 	 *
 	 * @see Matrix4#toTranslation(Vector3)
 	 */
 	public void toTranslation(float x, float y, float z) {
 		values[M11] = 1f; values[M12] = 0f; values[M13] = 0f; values[M14] = 0f;
 		values[M21] = 0f; values[M22] = 1f; values[M23] = 0f; values[M24] = 0f;
 		values[M31] = 0f; values[M32] = 0f; values[M33] = 1f; values[M34] = 0f;
 		values[M41] = x;  values[M42] = y;  values[M43] = z;  values[M44] = 1f;
 	}
 	
 	/**
 	 * Wandelt die Matrix in eine Skalierungsmatrix um
      *
      * <h2>Form der Matrix</h2>
      * <pre>
      * +---------+---+
      * |Sx  0  0 | 0 |
      * | 0 Sy  0 | 0 |
      * | 0  0 Sz | 0 |
      * +---------+---+
      * | 0  0  0 | 1 |
      * +---------+---+
      * </pre>
 	 *
 	 * @param factors Die Skalierungsfaktoren
 	 *
 	 * @see Matrix4#toScaling(float, float, float)
 	 * @see Matrix4#toScaling(float)
 	 */
 	public void toScaling(@NotNull final Vector3 factors) {
 		values[M11] = factors.x; values[M12] = 0f; values[M13] = 0f; values[M14] = 0f;
 		values[M21] = 0f; values[M22] = factors.y; values[M23] = 0f; values[M24] = 0f;
 		values[M31] = 0f; values[M32] = 0f; values[M33] = factors.z; values[M34] = 0f;
 		values[M41] = 0f; values[M42] = 0f; values[M43] = 0f; values[M44] = 1f;
 	}
 	
 	/**
 	 * Wandelt die Matrix in eine Skalierungsmatrix um
      *
      * <h2>Form der Matrix</h2>
      * <pre>
      * +---------+---+
      * | x  0  0 | 0 |
      * | 0  y  0 | 0 |
      * | 0  0  z | 0 |
      * +---------+---+
      * | 0  0  0 | 1 |
      * +---------+---+
      * </pre>
 	 *
 	 * @param x X-Skalierungsfaktor
 	 * @param y Y-Skalierungsfaktor
 	 * @param z Z-Skalierungsfaktor
 	 *
 	 * @see Matrix4#toScaling(Vector3)
 	 * @see Matrix4#toScaling(float)
 	 */
 	public void toScaling(float x, float y, float z) {
 		values[M11] = x;  values[M12] = 0f; values[M13] = 0f; values[M14] = 0f;
 		values[M21] = 0f; values[M22] = y;  values[M23] = 0f; values[M24] = 0f;
 		values[M31] = 0f; values[M32] = 0f; values[M33] = z;  values[M34] = 0f;
 		values[M41] = 0f; values[M42] = 0f; values[M43] = 0f; values[M44] = 1f;
 	}	
 	
 	/**
 	 * Wandelt die Matrix in eine Skalierungsmatrix um
      *
      * <h2>Form der Matrix</h2>
      * <pre>
      * +---------+---+
      * | s  0  0 | 0 |
      * | 0  s  0 | 0 |
      * | 0  0  s | 0 |
      * +---------+---+
      * | 0  0  0 | 1 |
      * +---------+---+
      * </pre>
 	 * @param s Der Skalierungsfaktor
 	 * @see Matrix4#toScaling(float)
 	 */
 	public void toScaling(float s) {
 		values[M11] = s;  values[M12] = 0f; values[M13] = 0f; values[M14] = 0f;
 		values[M21] = 0f; values[M22] = s;  values[M23] = 0f; values[M24] = 0f;
 		values[M31] = 0f; values[M32] = 0f; values[M33] = s;  values[M34] = 0f;
 		values[M41] = 0f; values[M42] = 0f; values[M43] = 0f; values[M44] = 1f;
 	}
 	
 	/**
 	 * Transformiert einen Vektor mittels dieser Matrix
 	 *
 	 * @param vector Der zu transformierende Vektor
 	 * @param w Der 4-dimensionale Überhang
 	 * @return Der transformierte Vektor
 	 * @see Matrix4#transform(Vector3)
 	 */
 	@NotNull
 	public Vector3 transform(@NotNull final Vector3 vector, float w) {
         Vector3 v = vector.clone();
        transformInPlace(v);
         return v;
 	}
 
     /**
      * Transformiert einen Vektor mittels dieser Matrix
      *
      * @param vector Der zu transformierende Vektor
      * @param w      Der 4-dimensionale Überhang
      * @return Der transformierte Vektor
      * @see Matrix4#transform(Vector3)
      */
     public void transformInPlace(@NotNull Vector3 vector, float w) {
         float x = (getAt(0, 0) * vector.x) + (getAt(1, 0) * vector.y) + (getAt(2, 0) * vector.z) + (getAt(3, 0) * w);
         float y = (getAt(0, 1) * vector.x) + (getAt(1, 1) * vector.y) + (getAt(2, 1) * vector.z) + (getAt(3, 1) * w);
         float z = (getAt(0, 2) * vector.x) + (getAt(1, 2) * vector.y) + (getAt(2, 2) * vector.z) + (getAt(3, 2) * w);
         float w2 = (getAt(0, 3) * vector.x) + (getAt(1, 3) * vector.y) + (getAt(2, 3) * vector.z) + (getAt(3, 3) * w);
 
         vector.set(x / w2, y / w2, z / w2);
     }
 	
 	/**
 	 * Transformiert einen Vektor mittels dieser Matrix unter der Annahme w=0
 	 * 
 	 * @param vector Der zu transformierende Vektor
 	 * @return Der transformierte Vektor
 	 * @see Matrix4#transform(Vector3, float)
 	 */
 	@ReturnsCachedValue
 	public Vector3 transform(final @NotNull Vector3 vector) {
         Vector3 v = vector.clone();
         transformInPlace(v);
 		return v;
 	}
 
     /**
      * Transformiert einen Vektor mittels dieser Matrix unter der Annahme w=0
      *
      * @param vector Der zu transformierende Vektor
      * @return Der transformierte Vektor
      * @see Matrix4#transform(Vector3, float)
      */
     public void transformInPlace(@NotNull Vector3 vector) {
         float x = (getAt(0, 0) * vector.x) + (getAt(1, 0) * vector.y) + (getAt(2, 0) * vector.z) + (getAt(3, 0));
         float y = (getAt(0, 1) * vector.x) + (getAt(1, 1) * vector.y) + (getAt(2, 1) * vector.z) + (getAt(3, 1));
         float z = (getAt(0, 2) * vector.x) + (getAt(1, 2) * vector.y) + (getAt(2, 2) * vector.z) + (getAt(3, 2));
         float w = (getAt(0, 3) * vector.x) + (getAt(1, 3) * vector.y) + (getAt(2, 3) * vector.z) + (getAt(3, 3));
 
         vector.set(x / w, y / w, z / w);
     }
 	
 	/**
 	 * Erzeugt eine Matrix zur Rotation um die X-Achse
 	 * @param theta Der Winkel
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerRPY(float, float, float)
 	 * @see Matrix4#getRotationEulerZXZ(float, float, float)
 	 * @see Matrix4#getRotationEulerZYZ(float, float, float)
 	 * @see Matrix4#getRotationY(float)
 	 * @see Matrix4#getRotationZ(float)
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 * @see Matrix4#getProgressiveRotation(float, float, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationX(float theta) {
 		float cos = (float)Math.cos(theta);
 		float sin = (float)Math.sin(theta);
 		return createNew().set(
 			1.0f, 0.0f, 0.0f, 0.0f,
 			0.0f, cos, sin, 0.0f,
 			0.0f, -sin, cos, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	/**
 	 * Erzeugt eine Matrix zur Rotation um die X-Achse
 	 *
 	 * @param cosTheta Der Kosinus des Winkels
 	 * @param sinTheta Der Sinus des Winkels
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationX(float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationX(float cosTheta, float sinTheta) {
 		return createNew().set(
 			1.0f, 0.0f, 0.0f, 0.0f,
 			0.0f, cosTheta, sinTheta, 0.0f,
 			0.0f, -sinTheta, cosTheta, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	/**
 	 * Erzeugt eine Matrix zur Rotation um die Y-Achse
 	 *
 	 * @param theta Der Winkel
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerRPY(float, float, float)
 	 * @see Matrix4#getRotationEulerZXZ(float, float, float)
 	 * @see Matrix4#getRotationEulerZYZ(float, float, float)
 	 * @see Matrix4#getRotationX(float)
 	 * @see Matrix4#getRotationZ(float)
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 * @see Matrix4#getProgressiveRotation(float, float, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationY(float theta) {
 		float cos = (float)Math.cos(theta);
 		float sin = (float)Math.sin(theta);
 		return createNew().set(
 			cos, 0.0f, -sin, 0.0f,
 			0.0f, 1.0f, 0.0f, 0.0f,
 			-sin, 0.0f, cos, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 	
 	/**
 	 * Erzeugt eine Matrix zur Rotation um die Y-Achse
 	 *
 	 * @param cosTheta Der Kosinus des Winkels
 	 * @param sinTheta Der Sinus des Winkels
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationY(float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationY(float cosTheta, float sinTheta) {
 		return createNew().set(
 			cosTheta, 0.0f, -sinTheta, 0.0f,
 			0.0f, 1.0f, 0.0f, 0.0f,
 			-sinTheta, 0.0f, cosTheta, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	/**
 	 * Erzeugt eine Matrix zur Rotation um die Z-Achse
 	 *
 	 * @param theta Der Winkel
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerRPY(float, float, float)
 	 * @see Matrix4#getRotationEulerZXZ(float, float, float)
 	 * @see Matrix4#getRotationEulerZYZ(float, float, float)
 	 * @see Matrix4#getRotationX(float)
 	 * @see Matrix4#getRotationY(float)
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 * @see Matrix4#getProgressiveRotation(float, float, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationZ(float theta) {
 		float cos = (float)Math.cos(theta);
 		float sin = (float)Math.sin(theta);
 		return createNew().set(
 			cos, sin, 0.0f, 0.0f,
 			-sin, cos, 0.0f, 0.0f,
 			0.0f, 0.0f, 1.0f, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	/**
 	 * Erzeugt eine Matrix zur Rotation um die Z-Achse
 	 *
 	 * @param cosTheta Der Kosinus des Winkels
 	 * @param sinTheta Der Sinus des Winkels
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationZ(float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationZ(float cosTheta, float sinTheta) {
 		return createNew().set(
 			cosTheta, sinTheta, 0.0f, 0.0f,
 			-sinTheta, cosTheta, 0.0f, 0.0f,
 			0.0f, 0.0f, 1.0f, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 	
 	/**
 	 * Erzeugt eine Matrix zur Rotation um eine Achse
 	 * @param axis Die Achse
 	 * @param theta Der Winkel
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerRPY(float, float, float)
 	 * @see Matrix4#getRotationEulerZXZ(float, float, float)
 	 * @see Matrix4#getRotationEulerZYZ(float, float, float)
 	 * @see Matrix4#getRotationX(float)
 	 * @see Matrix4#getRotationY(float)
 	 * @see Matrix4#getRotationZ(float)
 	 * @see Matrix4#getProgressiveRotation(float, float, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationAxisAngle(@NotNull Vector3 axis, float theta) {
 		float cos = (float)Math.cos(theta);
 		float sin = (float)Math.sin(theta);
 		return getRotationAxisAngle(axis, cos, sin);
 	}
 
 	/**
 	 * Erzeugt eine Matrix zur Rotation um eine Achse
 	 *
 	 * @param axis Die Achse
 	 * @param cosTheta Der Kosinus des Winkels
 	 * @param sinTheta Der Sinus des Winkels
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationAxisAngle(@NotNull Vector3 axis, float cosTheta, float sinTheta) {
 		// pre-calculate squared
 		float xx = axis.x * axis.x;
 		float yy = axis.y * axis.y;
 		float zz = axis.z * axis.z;
 
 		// pre-calculate axis combinations
 		float xy = axis.x * axis.y;
 		float xz = axis.x * axis.z;
 		float yz = axis.y * axis.z;
 
 		// pre-calculate axes and angle functions
 		float xsin = axis.x * sinTheta;
 		float ysin = axis.y * sinTheta;
 		float zsin = axis.z * sinTheta;
 		float xcos = axis.x * cosTheta;
 		float ycos = axis.y * cosTheta;
 
 		/*
 		return new Matrix4D(
 			xx * (1 - cos) + cos, xy * (1 - cos) + zsin, xz * (1 - cos) - ysin, 0.0d,
 			xy * (1 - cos) - zsin, yy * (1 - cos) + cos, yz * (1 - cos) + xsin, 0.0d,
 			xz * (1 - cos) + ysin, yz * (1 - cos) + xsin, zz * (1 - cos) + cos, 0.0d,
 			0.0d, 0.0d, 0.0d, 1.0d);
 		*/
 
 		return createNew().set(
 			xx - axis.x * xcos + cosTheta, xy - axis.y * xcos + zsin, xz - axis.y * xcos - ysin, 0.0f,
 			xy - axis.y * xcos - zsin, yy - axis.y * ycos + cosTheta, yz - axis.z * ycos + xsin, 0.0f,
 			xz - axis.z * xcos + ysin, yz - axis.z * ycos + xsin, zz - zz * cosTheta + cosTheta, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 	
 	/**
 	 * Erzeugt eine Matrix zur progressiven Rotation basierend auf Winkelgeschwindigkeit
 	 *
 	 * @param deltaX Winkelgeschwindigkeit in X-Richtung
 	 * @param deltaY Winkelgeschwindigkeit in Y-Richtung
 	 * @param deltaZ Winkelgeschwindigkeit in Z-Richtung
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerRPY(float, float, float)
 	 * @see Matrix4#getRotationEulerZXZ(float, float, float)
 	 * @see Matrix4#getRotationEulerZYZ(float, float, float)
 	 * @see Matrix4#getRotationX(float)
 	 * @see Matrix4#getRotationY(float)
 	 * @see Matrix4#getRotationZ(float)
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 */
 	@NotNull
 	public static Matrix4 getProgressiveRotation(float deltaX, float deltaY, float deltaZ) {
 		return createNew().set(
 			0.0f, -deltaZ, deltaY, 0.0f,
 			deltaZ, 0.0f, -deltaX, 0.0f,
 			-deltaY, deltaX, 0.0f, 0.0f,
 			0.0f, 0.0f, 0.0f, 1.0f);
 	}
 	
 	/**
 	 * Erzeugt eine Matrix zur Rotation gemäß Euler-ZXZ
 	 *
 	 * @param z Der Winkel um die Z-Achse
 	 * @param x1 Der Winkel um die X'-Achse
 	 * @param z2 Der Winkel um die Z''-Achse
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerRPY(float, float, float)
 	 * @see Matrix4#getRotationEulerZYZ(float, float, float)
 	 * @see Matrix4#getRotationX(float)
 	 * @see Matrix4#getRotationY(float)
 	 * @see Matrix4#getRotationZ(float)
 	 * @see Matrix4#getProgressiveRotation(float, float, float)
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationEulerZXZ(float z, float x1, float z2) {
 		float cz = (float)Math.cos(z);
 		float sz = (float)Math.sin(z);
 		
 		float cx1 = (float)Math.cos(x1);
 		float sx1 = (float)Math.sin(x1);
 		
 		float cz2 = (float)Math.cos(z2);
 		float sz2 = (float)Math.sin(z2);
 		
 		return createNew().set(
 				cz*cz2 - sz*cx1*sz2, -cz*sz2-sz*cx1*cz2, sz*sx1, 0f,
 				sz*cz2 + cz*cx1*sz2,  cz*cx1*cz2-sz*sz2, -cz*sx1, 0f,
 				sx1*sz2, sx1*cz2, cx1, 0f,
 				0f, 0f, 0f, 1f
 		);
 			
 	}
 
 	/**
 	 * Erzeugt eine Matrix zur Rotation gemäß Euler-ZYZ
 	 *
 	 * @param z Der Winkel um die Z-Achse
 	 * @param y1 Der Winkel um die Y'-Achse
 	 * @param z2 Der Winkel um die Z''-Achse
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerRPY(float, float, float)
 	 * @see Matrix4#getRotationEulerZXZ(float, float, float)
 	 * @see Matrix4#getRotationX(float)
 	 * @see Matrix4#getRotationY(float)
 	 * @see Matrix4#getRotationZ(float)
 	 * @see Matrix4#getProgressiveRotation(float, float, float)
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationEulerZYZ(float z, float y1, float z2) {
 		float cz = (float)Math.cos(z);
 		float sz = (float)Math.sin(z);
 		
 		float cy1 = (float)Math.cos(y1);
 		float sy1 = (float)Math.sin(y1);
 		
 		float cz2 = (float)Math.cos(z2);
 		float sz2 = (float)Math.sin(z2);
 		
 		return createNew().set(
 				-sz*sz2+cz*cy1*cz2, -sz*cz2-cz*cy1*sz2, cz*sy1, 0f,
 				 cz*sz2+sz*cy1*cz2,  cz*cz2-sz*cy1*sz2, sz*sy1, 0f,
 				-sy1*cz2,            sy1*sz2,           cy1,    0f,
 				0f, 0f, 0f, 1f
 		);
 			
 	}
 
 	/**
 	 * Erzeugt eine Matrix zur Rotation gemäß Euler-Roll-Pitch-Yaw
 	 *
 	 * @param rollX Der Rollwinkel
 	 * @param pitchY Der Nickwinkel
 	 * @param yawZ Der Gierwinkel
 	 * @return Die Rotationsmatrix
 	 *
 	 * @see Matrix4#getRotationEulerZXZ(float, float, float)
 	 * @see Matrix4#getRotationEulerZYZ(float, float, float)
 	 * @see Matrix4#getRotationX(float)
 	 * @see Matrix4#getRotationY(float)
 	 * @see Matrix4#getRotationZ(float)
 	 * @see Matrix4#getProgressiveRotation(float, float, float)
 	 * @see Matrix4#getRotationAxisAngle(Vector3, float)
 	 */
 	@NotNull
 	public static Matrix4 getRotationEulerRPY(float rollX, float pitchY, float yawZ) {
 		float cr = (float)Math.cos(rollX); // Φ
 		float sr = (float)Math.sin(rollX);
 		
 		float cp = (float)Math.cos(pitchY); // Θ
 		float sp = (float)Math.sin(pitchY);
 		
 		float cy = (float)Math.cos(yawZ); // Ψ
 		float sy = (float)Math.sin(yawZ);
 		
 		return createNew().set(
 				cp*cy, cp*sy, -sp, 0f,
 				sr*sp*cy-cr*sy, sr*sp*sy + cr*cy, sr*cp, 0f,
 				cr*sp*cy+sr*sy, cr*sp*sy-sr*cy, cr*cp, 0f,
 				0f, 0f, 0f, 1f
 		);
 	}
 	
 	/**
 	 * Bezieht eine vollständige Transformationsmatrix.
 	 * Die Transformationen werden in der Reihenfolge Skalierung, Rotation, Translation angewandt,
 	 * 
 	 * @param scaling Der Skalierungsvektor
 	 * @param rotation Der Rotationsvektor (roll-pitch-yaw)
 	 * @param translation Der translationsvektor
 	 * @return Die Transformationsmatrix
 	 */
 	public static Matrix4 getTransformation(Vector3 scaling, Vector3 rotation, Vector3 translation) {
 		float cr = (float)Math.cos(rotation.x); // Φ
 		float sr = (float)Math.sin(rotation.x);
 		
 		float cp = (float)Math.cos(rotation.y); // Θ
 		float sp = (float)Math.sin(rotation.y);
 		
 		float cy = (float)Math.cos(rotation.z); // Ψ
 		float sy = (float)Math.sin(rotation.z);
 		
 		return createNew().set(
 				scaling.x* cp*cy, 				scaling.x* cp*sy, 				scaling.x* (-sp),	0f,
 				-scaling.y* (sr*sp*cy - cr*sy), scaling.y* (sr*sp*sy + cr*cy), 	scaling.y* sr*cp, 	0f,
 				scaling.z* (cr*sp*cy + sr*sy), 	scaling.z* (cr*sp*sy - sr*cy), 	scaling.z* cr*cp, 	0f,
 				translation.x, 					translation.y, 					translation.z, 		1f
 		);
 	}
 	
 	/**
 	 * Bezieht eine vollständige Transformationsmatrix.
 	 * Die Transformationen werden in der Reihenfolge Skalierung, Rotation, Translation angewandt,
 	 * 
 	 * @param scaling Der Skalierungsvektor
 	 * @param rotation Der Rotationsvektor (roll-pitch-yaw)
 	 * @param translation Der translationsvektor
 	 * @return Die Transformationsmatrix
 	 */
 	public static Matrix4 getTransformation(float scaling, Vector3 rotation, Vector3 translation) {
 		float cr = (float)Math.cos(rotation.x); // Φ
 		float sr = (float)Math.sin(rotation.x);
 		
 		float cp = (float)Math.cos(rotation.y); // Θ
 		float sp = (float)Math.sin(rotation.y);
 		
 		float cy = (float)Math.cos(rotation.z); // Ψ
 		float sy = (float)Math.sin(rotation.z);
 		
 		return createNew().set(
 				scaling* cp*cy, 				scaling* cp*sy, 				scaling* (-sp),		0f,
 				-scaling* (sr*sp*cy - cr*sy), 	scaling* (sr*sp*sy + cr*cy), 	scaling* sr*cp, 	0f,
 				scaling* (cr*sp*cy + sr*sy), 	scaling* (cr*sp*sy - sr*cy), 	scaling* cr*cp, 	0f,
 				translation.x, 					translation.y, 					translation.z, 		1f
 		);
 	}
 	
 	/**
 	 * Bezieht eine vollständige Transformationsmatrix.
 	 * Die Transformationen werden in der Reihenfolge Rotation, Translation angewandt.
 	 * 
 	 * @param rotation Der Rotationsvektor (roll-pitch-yaw)
 	 * @param translation Der translationsvektor
 	 * @return Die Transformationsmatrix
 	 */
 	public static Matrix4 getTransformation(Vector3 rotation, Vector3 translation) {
 		float cr = (float)Math.cos(rotation.x); // Φ
 		float sr = (float)Math.sin(rotation.x);
 		
 		float cp = (float)Math.cos(rotation.y); // Θ
 		float sp = (float)Math.sin(rotation.y);
 		
 		float cy = (float)Math.cos(rotation.z); // Ψ
 		float sy = (float)Math.sin(rotation.z);
 		
 		return createNew().set(
 				cp*cy, 					cp*sy, 					(-sp),			0f,
 				-(sr*sp*cy - cr*sy), 	(sr*sp*sy + cr*cy), 	sr*cp, 			0f,
 				(cr*sp*cy + sr*sy), 	(cr*sp*sy - sr*cy), 	cr*cp, 			0f,
 				translation.x, 			translation.y, 			translation.z, 	1f
 		);
 	}
 }
