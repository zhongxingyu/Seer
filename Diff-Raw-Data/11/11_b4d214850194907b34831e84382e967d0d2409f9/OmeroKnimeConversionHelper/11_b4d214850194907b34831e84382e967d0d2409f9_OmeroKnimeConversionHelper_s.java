 /*
  * ------------------------------------------------------------------------
  *
  *  Copyright (C) 2003 - 2013
  *  University of Konstanz, Germany and
  *  KNIME GmbH, Konstanz, Germany
  *  Website: http://www.knime.org; Email: contact@knime.org
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License, Version 3, as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, see <http://www.gnu.org/licenses>.
  *
  *  Additional permission under GNU GPL version 3 section 7:
  *
  *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
  *  Hence, KNIME and ECLIPSE are both independent programs and are not
  *  derived from each other. Should, however, the interpretation of the
  *  GNU GPL Version 3 ("License") under any applicable laws result in
  *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
  *  you the additional permission to use and propagate KNIME together with
  *  ECLIPSE with only the license terms in place for ECLIPSE applying to
  *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
  *  license terms of ECLIPSE themselves allow for the respective use and
  *  propagation of ECLIPSE together with KNIME.
  *
  *  Additional permission relating to nodes for KNIME that extend the Node
  *  Extension (and in particular that are based on subclasses of NodeModel,
  *  NodeDialog, and NodeView) and that only interoperate with KNIME through
  *  standard APIs ("Nodes"):
  *  Nodes are deemed to be separate and independent programs and to not be
  *  covered works.  Notwithstanding anything to the contrary in the
  *  License, the License does not apply to Nodes, you are not required to
  *  license Nodes under the License, and you are granted a license to
  *  prepare and propagate Nodes, in each case even if such Nodes are
  *  propagated with or for interoperation with KNIME.  The owner of a Node
  *  may freely choose the license terms applicable to such Node, including
  *  when such Node is propagated with or for interoperation with KNIME.
  * --------------------------------------------------------------------- *
  *
  */
 package org.knime.knip.omero.insight;
 
 import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
 import net.imglib2.img.basictypeaccess.array.ByteArray;
 import net.imglib2.img.basictypeaccess.array.DoubleArray;
 import net.imglib2.img.basictypeaccess.array.IntArray;
 import net.imglib2.img.basictypeaccess.array.ShortArray;
 import net.imglib2.meta.Axes;
 import net.imglib2.meta.AxisType;
 import net.imglib2.type.numeric.RealType;
 import net.imglib2.type.numeric.integer.ByteType;
 import net.imglib2.type.numeric.integer.IntType;
 import net.imglib2.type.numeric.integer.ShortType;
 import net.imglib2.type.numeric.integer.UnsignedByteType;
 import net.imglib2.type.numeric.integer.UnsignedIntType;
 import net.imglib2.type.numeric.integer.UnsignedShortType;
 import net.imglib2.type.numeric.real.DoubleType;
 
 import org.knime.knip.omero.omerojava.Plane1D;
 import org.openmicroscopy.shoola.env.data.DSAccessException;
 import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
 
 import pojos.PixelsData;
 
 /**
  * collection of static helper methods for image conversion between OMERO and
  * ImgLib.
 * 
  * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
  * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
  * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
  *         Zinsmaier</a>
  */
 public final class OmeroKnimeConversionHelper {
 
 	/**
 	 * utility class.
 	 */
 	private OmeroKnimeConversionHelper() {
 
 	}
 
 	/**
 	 * Converts OMERO image data in form of a Plane1D into a ImgLib image.
	 * 
 	 * @param typeString
 	 *            string identifier of a image type
 	 *            {@link org.knime.knip.omero.omerojava.PixelTypes PixelTypes}
 	 * @param p1d
 	 *            a Plane1D that contains the data for the conversion
 	 * @return a data access array of the correct data type
 	 */
 	@SuppressWarnings("rawtypes")
 	public static ArrayDataAccess makeDataAccessArray(final String typeString,
 			final Plane1D p1d) {
 		ArrayDataAccess<?> access;
 
 		if (typeString.equals(PixelsData.INT8_TYPE)) {
 			access = new ByteArray(p1d.getPixelsArrayAsByte());
 		} else if (typeString.equals(PixelsData.INT16_TYPE)) {
 			access = new ShortArray(p1d.getPixelsArrayAsShort());
 		} else if (typeString.equals(PixelsData.INT32_TYPE)) {
 			access = new IntArray(p1d.getPixelsArrayAsInt());
 		} else if (typeString.equals(PixelsData.UINT8_TYPE)) {
 			access = new ByteArray(p1d.getPixelsArrayAsByte());
 		} else if (typeString.equals(PixelsData.UINT16_TYPE)) {
 			access = new ShortArray(p1d.getPixelsArrayAsShort());
 		} else if (typeString.equals(PixelsData.UINT32_TYPE)) {
 			access = new IntArray(p1d.getPixelsArrayAsInt());
 		} else if (typeString.equals(PixelsData.DOUBLE_TYPE)) { // FLOAT_TYPE ==
 																// DOUBLE_TYPE
 			access = new DoubleArray(p1d.getPixelsArrayAsDouble());
 		} else {
 			throw new IllegalArgumentException(
 					"The given pixel type is not supported yet.");
 		}
 		return access;
 	}
 
 	/**
 	 * Gives a ImgLib RealType for a given OMERO type string
	 * 
 	 * @param typeString
 	 *            string identifier of a image type
 	 *            {@link org.knime.knip.omero.omerojava.PixelTypes PixelTypes}
 	 * @return a matching RealType
 	 * @throws DSOutOfServiceException
 	 * @throws DSAccessException
 	 */
 	@SuppressWarnings("rawtypes")
 	public static RealType makeType(final String typeString) {
 		final RealType type;
 
 		if (typeString.equals(PixelsData.INT8_TYPE)) {
 			type = new ByteType();
 		} else if (typeString.equals(PixelsData.INT16_TYPE)) {
 			type = new ShortType();
 		} else if (typeString.equals(PixelsData.INT32_TYPE)) {
 			type = new IntType();
 		} else if (typeString.equals(PixelsData.UINT8_TYPE)) {
 			type = new UnsignedByteType();
 		} else if (typeString.equals(PixelsData.UINT16_TYPE)) {
 			type = new UnsignedShortType();
 		} else if (typeString.equals(PixelsData.UINT32_TYPE)) {
 			type = new UnsignedIntType();
 		} else if (typeString.equals(PixelsData.DOUBLE_TYPE)) { // FLOAT_TYPE ==
 																// DOUBLE_TYPE
 			type = new DoubleType();
 		} else {
 			throw new IllegalArgumentException(
 					"The given pixel type is not supported yet.");
 		}
 		return type;
 	}
 
 	/**
 	 * @return standard OMEREO axis with respect to name and order X,Y,Z,T,C
 	 */
 	public static AxisType[] getAxes() {
 		return new AxisType[] { Axes.get("X"), Axes.get("Y"), Axes.get("Z"),
				Axes.get("T"), Axes.get("C") };
 	}
 }
