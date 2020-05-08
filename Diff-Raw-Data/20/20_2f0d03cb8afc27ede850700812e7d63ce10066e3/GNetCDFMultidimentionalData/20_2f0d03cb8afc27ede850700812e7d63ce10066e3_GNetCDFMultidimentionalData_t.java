 /*
 
  IGO Software SL  -  info@igosoftware.es
 
  http://www.glob3.org
 
 -------------------------------------------------------------------------------
  Copyright (c) 2010, IGO Software SL
  All rights reserved. 
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the IGO Software SL nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL IGO Software SL BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -------------------------------------------------------------------------------
 
 */
 
 
 package es.igosoftware.experimental.ndimensional;
 
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 import es.igosoftware.euclid.colors.GColorI;
 import es.igosoftware.euclid.colors.GColorPrecision;
 import es.igosoftware.euclid.colors.IColor;
 import es.igosoftware.euclid.projection.GProjection;
 import es.igosoftware.euclid.vector.GVector2D;
 import es.igosoftware.euclid.vector.GVector3D;
 import es.igosoftware.euclid.vector.GVectorPrecision;
 import es.igosoftware.euclid.vector.IVector2;
 import es.igosoftware.euclid.vector.IVector3;
 import es.igosoftware.euclid.verticescontainer.GVertex3Container;
 import es.igosoftware.util.GAssert;
 import es.igosoftware.util.GCollections;
 import es.igosoftware.util.GHolder;
 import es.igosoftware.util.GMath;
 import es.igosoftware.util.GRange;
 import es.igosoftware.util.GStringUtils;
 import es.igosoftware.util.IPredicate;
 import es.igosoftware.util.ITransformer;
 import es.igosoftware.utils.GPositionBox;
 import es.igosoftware.utils.GWWUtils;
 import gov.nasa.worldwind.geom.Angle;
 import gov.nasa.worldwind.geom.Position;
 import gov.nasa.worldwind.geom.Vec4;
 import gov.nasa.worldwind.globes.Globe;
 import gov.nasa.worldwind.render.DrawContext;
 
 
 public class GNetCDFMultidimentionalData
          implements
             IMultidimensionalData {
 
    private static final int BYTES_PER_VECTOR3F = 3 * 4; // x, y, z * float 
 
 
    public static class VectorVariable {
       private final String _name;
       private final String _uVariableName;
       private final String _vVariableName;
 
       private Variable     _uVariable;
       private Variable     _vVariable;
       private double       _uMissingValue;
       private double       _vMissingValue;
 
 
       public VectorVariable(final String name,
                             final String uVariableName,
                             final String vVariableName) {
          _name = name;
          _uVariableName = uVariableName;
          _vVariableName = vVariableName;
       }
 
 
       @Override
       public String toString() {
          return "VectorVariable [name=" + _name + ", U=" + _uVariableName + ", V=" + _vVariableName + "]";
       }
 
    }
 
 
    private class ValueVariable {
       private final Variable _variable;
       private final double   _missingValue;
       private GRange<Double> _range;
 
 
       private ValueVariable(final Variable variable,
                             final double missingValue) {
          _variable = variable;
          _missingValue = missingValue;
       }
 
 
       private GRange<Double> getRange() {
          if (_range == null) {
             _range = calculateRange(_variable);
          }
          return _range;
       }
    }
 
 
    private final String           _fileName;
    private final NetcdfFile       _ncFile;
    private final boolean          _verbose;
    private final GPositionBox     _box;
 
    private final Variable         _latitudeVariable;
    private final Variable         _longitudeVariable;
    private final Variable         _elevationVariable;
    private final Variable         _elevationThresholdVariable;
 
    private final Dimension        _timeDimension;
    private final int              _timeDimensionLength;
 
    private final String[]         _valueVariablesNames;
    private final ValueVariable[]  _valueVariables;
 
    private final boolean          _dynamicRange;
    private final VectorVariable[] _vectorVariables;
    private final List<Dimension>  _dimensions;
    /** Default arrow angle. */
    public static final Angle      DEFAULT_ARROW_ANGLE     = Angle.fromDegrees(45.0);
    /** Default arrow length, in meters. */
    public static final double     DEFAULT_ARROW_LENGTH    = 300;
    /** Default arrow base, in meters. */
    public static final double     DEFAULT_ARROW_BASE      = 150;
    /** Default maximum screen size of the arrowheads, in pixels. */
    public static final double     DEFAULT_MAX_SCREEN_SIZE = 20.0;
    /** The angle of the arrowhead tip. */
    protected Angle                arrowAngle              = DEFAULT_ARROW_ANGLE;
    /** The length, in meters, of the arrowhead, from tip to base. */
    protected double               arrowLength             = DEFAULT_ARROW_LENGTH;
    /** The base length, in meters, of the arrowhead, from left to right */
    protected double               arrowBase               = DEFAULT_ARROW_BASE;
    /** The maximum screen size, in pixels, of the direction arrowheads. */
    protected double               maxScreenSize           = DEFAULT_MAX_SCREEN_SIZE;
 
 
    public GNetCDFMultidimentionalData(final String fileName,
                                       final String longitudeVariableName,
                                       final String latitudeVariableName,
                                       final String elevationVariableName,
                                       final String elevationThresholdVariableName,
                                       final String[] valueVariablesNames,
                                       final GNetCDFMultidimentionalData.VectorVariable[] vectorVariables,
                                       final String timeDimensionName,
                                       final boolean dynamicRange,
                                       final boolean verbose) throws IOException {
       GAssert.notNull(fileName, "fileName");
       GAssert.notNull(longitudeVariableName, "longitudeVariableName");
       GAssert.notNull(latitudeVariableName, "latitudeVariableName");
       GAssert.notNull(elevationVariableName, "elevationVariableName");
       //GAssert.notEmpty(valueVariablesNames, "valueVariablesNames");
       GAssert.notEmpty(vectorVariables, "vectorVariables");
 
       _verbose = verbose;
 
       _dynamicRange = dynamicRange;
 
       _fileName = fileName;
       _ncFile = NetcdfFile.open(fileName);
 
       if (_verbose) {
          System.out.println(_ncFile);
       }
 
 
       _latitudeVariable = _ncFile.findVariable(latitudeVariableName);
       if (_latitudeVariable == null) {
          throw new RuntimeException("Can't find the latitude variable (\"" + latitudeVariableName + "\")");
       }
 
 
       _longitudeVariable = _ncFile.findVariable(longitudeVariableName);
       if (_longitudeVariable == null) {
          throw new RuntimeException("Can't find the longitude variable (\"" + longitudeVariableName + "\")");
       }
 
 
       _elevationVariable = _ncFile.findVariable(elevationVariableName);
       if (_elevationVariable == null) {
          throw new RuntimeException("Can't find the elevation variable (\"" + elevationVariableName + "\")");
       }
 
       if (elevationThresholdVariableName == null) {
          _elevationThresholdVariable = null;
       }
       else {
          _elevationThresholdVariable = _ncFile.findVariable(elevationThresholdVariableName);
          if (_elevationThresholdVariable == null) {
             throw new RuntimeException("Can't find the elevation threshold variable (\"" + elevationThresholdVariableName + "\")");
          }
       }
 
 
       /**
        * Auto-detect non-dimension variables in NetCDF need to also reject the vector component variables and only retain scalars
        */
       if (valueVariablesNames == null) {
          _valueVariablesNames = detectValueVariableNames(longitudeVariableName, latitudeVariableName, elevationVariableName);
       }
       else {
          _valueVariablesNames = valueVariablesNames;
       }
 
       _valueVariables = new ValueVariable[_valueVariablesNames.length];
       for (int i = 0; i < _valueVariablesNames.length; i++) {
          final String valueVariableName = _valueVariablesNames[i];
 
          final Variable valueVariable = _ncFile.findVariable(valueVariableName);
          if (valueVariable == null) {
             throw new RuntimeException("Can't find the value variable (\"" + valueVariableName + "\")");
          }
 
          final Attribute valueMissingValueAtt = valueVariable.findAttribute("missing_value");
          final double valueMissingValue = (valueMissingValueAtt == null) ? Double.NaN
                                                                         : valueMissingValueAtt.getNumericValue().doubleValue();
 
          _valueVariables[i] = new ValueVariable(valueVariable, valueMissingValue);
       }
 
       //      _valueVariable = _ncFile.findVariable(valueVariableName);
       //      if (_valueVariable == null) {
       //         throw new RuntimeException("Can't find the value variable (\"" + valueVariableName + "\")");
       //      }
 
       //      final Attribute valueMissingValueAtt = _valueVariable.findAttribute("missing_value");
       //      if (valueMissingValueAtt != null) {
       //         _valueMissingValue = valueMissingValueAtt.getNumericValue().doubleValue();
       //      }
       //      else {
       //         _valueMissingValue = Double.NaN;
       //      }
 
       /**
        * Auto-detect non-dimension variables in NetCDF, only retain vectors
        */
       if (vectorVariables == null) {
          //vectorVariables = detectVectorVariables(longitudeVariableName, latitudeVariableName, elevationVariableName);
       }
 
       for (final VectorVariable vectorVariable : vectorVariables) {
          final Variable uVariable = _ncFile.findVariable(vectorVariable._uVariableName);
          if (uVariable == null) {
             throw new RuntimeException("Can't find the variable (\"" + vectorVariable._uVariableName + "\")");
          }
          vectorVariable._uVariable = uVariable;
 
          final Attribute uMissingValueAtt = uVariable.findAttribute("missing_value");
          vectorVariable._uMissingValue = (uMissingValueAtt == null) ? Double.NaN
                                                                    : uMissingValueAtt.getNumericValue().doubleValue();
 
 
          final Variable vVariable = _ncFile.findVariable(vectorVariable._vVariableName);
          if (vVariable == null) {
             throw new RuntimeException("Can't find the variable (\"" + vectorVariable._vVariableName + "\")");
          }
          vectorVariable._vVariable = vVariable;
 
          final Attribute vMissingValueAtt = vVariable.findAttribute("missing_value");
          vectorVariable._vMissingValue = (vMissingValueAtt == null) ? Double.NaN
                                                                    : vMissingValueAtt.getNumericValue().doubleValue();
 
 
          if (!uVariable.getDimensions().equals(vVariable.getDimensions())) {
             throw new RuntimeException("Variable " + vectorVariable._uVariableName + " has different dimensions than "
                                        + vectorVariable._vVariableName);
          }
       }
       _vectorVariables = vectorVariables;
 
 
       _timeDimension = _ncFile.findDimension(timeDimensionName);
       if (_timeDimension == null) {
          throw new RuntimeException("Can't find the time dimension (\"" + timeDimensionName + "\")");
       }
       _timeDimensionLength = _timeDimension.getLength();
 
       _box = calculateBox();
 
       _dimensions = _ncFile.getDimensions();
 
       //      _valueRange = calculateRange(_valueVariable);
    }
 
 
    private VectorVariable[] detectVectorVariables(final String longitudeVariableName,
                                                   final String latitudeVariableName,
                                                   final String elevationVariableName) {
       // TODO Auto-generated method stub
       return null;
    }
 
 
    private String[] detectValueVariableNames(final String longitudeVariableName,
                                              final String latitudeVariableName,
                                              final String elevationVariableName) {
       final List<Variable> allVars = _ncFile.getVariables();
       final ArrayList<String> autoVarName = new ArrayList<String>();
       for (final Variable variable : allVars) {
          final String varName = variable.getName();
          if (!(latitudeVariableName.equals(varName) || elevationVariableName.equals(varName) || longitudeVariableName.equals(varName))) {
             //FIXME: Current code supports only 4-D data
             if (variable.getDimensions().size() == 4) {
                //FIXME: What is the correct way to detect vector variables ?
                final List<Attribute> allAttr = variable.getAttributes();
                boolean isVector = false;
                for (final Attribute attribute : allAttr) {
                   if (attribute.isString() && attribute.getName().contains("vector_components")) {
                      isVector = true;
                   }
                }
                autoVarName.add(varName);
             }
          }
       }
       return autoVarName.toArray(new String[1]);
    }
 
 
    @Override
    public GPositionBox getBox() {
       return _box;
    }
 
 
    private GPositionBox calculateBox() {
       final GRange<Double> latitudeRange = calculateRange(_latitudeVariable);
       final GRange<Double> longitudeRange = calculateRange(_longitudeVariable);
       final GRange<Double> elevationRange = calculateRange(_elevationVariable);
 
       final Position lower = new Position(Angle.fromDegrees(latitudeRange._lower), Angle.fromDegrees(longitudeRange._lower),
                elevationRange._lower);
       final Position upper = new Position(Angle.fromDegrees(latitudeRange._upper), Angle.fromDegrees(longitudeRange._upper),
                elevationRange._upper);
 
       return new GPositionBox(lower, upper);
    }
 
 
    private GRange<Double> calculateRange(final Variable var) {
 
       final long start = System.currentTimeMillis();
 
       final GRange<Double> range;
 
       final Attribute rangeAttribute = var.findAttribute("valid_range");
       if ((rangeAttribute != null) && !_dynamicRange) {
          System.out.println("  Found Range-Attribute=" + rangeAttribute + " in " + var.getName() + " (" + var.getDescription()
                             + ")");
 
          range = new GRange<Double>(rangeAttribute.getNumericValue(0).doubleValue(),
                   rangeAttribute.getNumericValue(1).doubleValue());
       }
       else {
 
          final List<Dimension> dimensions = var.getDimensions();
 
          @SuppressWarnings("unchecked")
          final List<Integer>[] ranges = (List<Integer>[]) new List<?>[dimensions.size()];
 
          int i = 0;
          for (final Dimension dimension : dimensions) {
             ranges[i++] = GCollections.rangeList(0, dimension.getLength() - 1);
          }
 
 
          final Attribute missingValueAtt = var.findAttribute("missing_value");
          final double missingValue = (missingValueAtt == null) ? Double.NaN : missingValueAtt.getNumericValue().doubleValue();
 
          final GHolder<Double> min = new GHolder<Double>(Double.POSITIVE_INFINITY);
          final GHolder<Double> max = new GHolder<Double>(Double.NEGATIVE_INFINITY);
 
          combination(new Processor<Integer>() {
             @Override
             public void process(final List<Integer> indices) {
 
                try {
                   final String section = indices.toString().substring(1, indices.toString().length() - 1);
                   final double value = var.read(section).getDouble(0);
 
                   if (!Double.isNaN(missingValue) && GMath.closeTo(value, missingValue)) {
                      return;
                   }
 
                   if (value > max.get()) {
                      max.set(value);
                   }
 
                   if (value < min.get()) {
                      min.set(value);
                   }
                }
                catch (final IOException e) {
                   e.printStackTrace();
                }
                catch (final InvalidRangeException e) {
                   e.printStackTrace();
                }
             }
          }, ranges);
 
 
          range = new GRange<Double>(min.get(), max.get());
       }
 
       if (_verbose) {
          final long elapsed = System.currentTimeMillis() - start;
          System.out.println("Range " + var.getName() + " (" + var.getDescription() + ")  " + range + "   calculated in "
                             + GStringUtils.getTimeMessage(elapsed));
       }
 
       return range;
    }
 
 
    private static interface Processor<T> {
       public void process(final List<T> values);
    }
 
 
    private static <T> void combination(final Processor<T> processor,
                                        final List<T>... sets) {
 
       if (sets.length == 0) {
          throw new RuntimeException("Can't process an empty array of values");
       }
 
       final List<T> emptyStack = Collections.emptyList();
       //FIXME: Safely back ground process
       pvtCombination(processor, emptyStack, sets);
 
    }
 
 
    private static <T> void pvtCombination(final Processor<T> processor,
                                           final List<T> stack,
                                           final List<T>... sets) {
 
       final int setsCount = sets.length;
 
 
       if (setsCount == 1) {
          final List<T> head = sets[0];
 
 
          for (final T each : head) {
             final List<T> newStack = new ArrayList<T>(stack.size() + 1);
             newStack.addAll(stack);
             newStack.add(each);
             processor.process(newStack);
          }
       }
       else {
          final List<T> head = sets[0];
 
          @SuppressWarnings("unchecked")
          final List<T>[] tail = new List[setsCount - 1];
          System.arraycopy(sets, 1, tail, 0, setsCount - 1);
 
          for (final T each : head) {
             final List<T> newStack = new ArrayList<T>(stack.size() + 1);
             newStack.addAll(stack);
             newStack.add(each);
 
             pvtCombination(processor, newStack, tail);
          }
       }
    }
 
 
    @Override
    public int getTimeDimensionLength() {
       return _timeDimensionLength;
    }
 
 
    private double get(final Variable variable,
                       final List<Dimension> valueDimensions,
                       final List<Integer> indices) throws IOException, InvalidRangeException {
 
       final StringBuffer section = new StringBuffer();
       final List<Dimension> dimensions = variable.getDimensions();
       for (final Dimension dimension : dimensions) {
          final String dimensionName = dimension.getName();
          for (int i = 0; i < valueDimensions.size(); i++) {
             final Dimension valueDimension = valueDimensions.get(i);
             if (valueDimension.getName().equals(dimensionName)) {
                if (section.length() != 0) {
                   section.append(",");
                }
                section.append(indices.get(i));
             }
          }
       }
 
       return variable.read(section.toString()).getDouble(0);
    }
 
 
    @Override
    public String getName() {
       final String title = _ncFile.getTitle();
       return (title == null) ? _fileName : title;
    }
 
 
    private static final GColorI[] RAMP = new GColorI[] { GColorI.CYAN, GColorI.GREEN, GColorI.YELLOW, GColorI.RED };
 
 
    private static GColorI interpolateColorFromRamp(final GColorI colorFrom,
                                                    final GColorI[] ramp,
                                                    final float alpha) {
       final float rampStep = 1f / ramp.length;
 
       final int toI;
       if (GMath.closeTo(alpha, 1)) {
          toI = ramp.length - 1;
       }
       else {
          toI = (int) (alpha / rampStep);
       }
 
       final GColorI from;
       if (toI == 0) {
          from = colorFrom;
       }
       else {
          from = ramp[toI - 1];
       }
 
       final float colorAlpha = (alpha % rampStep) / rampStep;
       return from.interpolatedTo(ramp[toI], colorAlpha);
    }
 
 
    private GColorI colorizeValue(final double value,
                                  final GRange<Double> range) {
       final float alpha = (float) GMath.clamp((value - range._lower) / (range._upper - range._lower), 0, 1);
 
       return interpolateColorFromRamp(GColorI.BLUE, RAMP, alpha);
    }
 
 
    private static final IVector2<?> reference = new GVector2D(1, 0);
 
 
    private GColorI colorizeVectorByAngle(final double u,
                                          final double v) {
       final GVector2D that = new GVector2D(u, v);
       //      final double angle = reference.angle(that);
       final double angle = Math.acos(reference.dot(that) / (reference.length() * that.length()));
 
       final float alpha = (float) (angle / Math.PI);
 
       return interpolateColorFromRamp(GColorI.BLUE, RAMP, alpha);
    }
 
 
    @Override
    public List<String> getAvailableValueVariablesNames() {
       final List<String> result = new ArrayList<String>(_valueVariablesNames.length);
 
       for (final String variableName : _valueVariablesNames) {
          final Variable variable = _ncFile.findVariable(variableName);
 
          final Attribute longNameAttribute = variable.findAttribute("long_name");
 
          result.add((longNameAttribute == null) ? variable.getName() : longNameAttribute.getStringValue());
       }
 
       //      return _valueVariablesNames;
       return result;
    }
 
 
    @Override
    public List<String> getAvailableVectorVariablesNames() {
       if (_vectorVariables == null) {
          return null;
       }
 
       final List<String> result = new ArrayList<String>(_vectorVariables.length);
       for (final VectorVariable variable : _vectorVariables) {
          result.add(variable._name);
       }
 
       return result;
    }
 
 
    private ValueVariable findValueVariable(final String variableName) {
       for (final ValueVariable each : _valueVariables) {
          if (each._variable.getName().equals(variableName) || each._variable.getDescription().equals(variableName)) {
             return each;
          }
       }
       throw new IllegalArgumentException("Can't find value variable named \"" + variableName + "\"");
    }
 
 
    private IMultidimensionalData.PointsCloud calculateValuePointsCloud(final ValueVariable valueVariable,
                                                                        final int time,
                                                                        final Globe globe,
                                                                        final double verticalExaggeration,
                                                                        final Vec4 referencePoint,
                                                                        final Map<String, GRange<Integer>> dimensionsRanges,
                                                                        final float alpha) {
 
       int initialCapacity = 1;
       final List<Dimension> dimensions = valueVariable._variable.getDimensions();
       for (final Dimension dimension : dimensions) {
          if (!dimension.getName().equals(_timeDimension.getName())) {
             initialCapacity *= dimension.getLength();
          }
       }
 
       final GVertex3Container vertexContainer = new GVertex3Container(GVectorPrecision.FLOAT, GColorPrecision.INT,
                GProjection.EUCLID, initialCapacity, false, 0, true, GColorI.WHITE, false, null);
 
 
       @SuppressWarnings("unchecked")
       final List<Integer>[] ranges = (List<Integer>[]) new List<?>[dimensions.size()];
 
       int i = 0;
       for (final Dimension dimension : dimensions) {
          final List<Integer> range;
          if (dimension.getName().equals(_timeDimension.getName())) {
             range = Arrays.asList(time);
          }
          else {
             //            range = GCollections.rangeList(0, dimension.getLength() - 1);
             final GRange<Integer> dimensionRange = dimensionsRanges.get(dimension.getName());
             range = GCollections.rangeList(dimensionRange._lower, dimensionRange._upper);
          }
          //         System.out.println(dimension.getName() + " " + range);
          ranges[i++] = range;
       }
 
       //      final GHolder<Integer> removedCounter = new GHolder<Integer>(0);
 
       combination(new Processor<Integer>() {
          @Override
          public void process(final List<Integer> indices) {
             try {
 
                final String section = indices.toString().substring(1, indices.toString().length() - 1);
                final double value = valueVariable._variable.read(section).getDouble(0);
 
                if (!Double.isNaN(valueVariable._missingValue) && GMath.closeTo(value, valueVariable._missingValue)) {
                   return;
                }
 
                boolean removedPoint = false;
                final double z = get(_elevationVariable, dimensions, indices);
                if (_elevationThresholdVariable != null) {
                   final double elevationThreshold = get(_elevationThresholdVariable, dimensions, indices);
                   if (z > elevationThreshold) {
                      removedPoint = true;
                      return;
                   }
                }
 
 
                final double x = get(_longitudeVariable, dimensions, indices);
                final double y = get(_latitudeVariable, dimensions, indices);
 
                final Position position = new Position(Angle.fromDegrees(y), Angle.fromDegrees(x), z);
                final Vec4 point4 = GWWUtils.toVec4(position, globe, verticalExaggeration);
 
                final GVector3D point = new GVector3D(point4.x - referencePoint.x, point4.y - referencePoint.y, point4.z
                                                                                                                - referencePoint.z);
 
                final GColorI color;
                if (removedPoint) {
                   color = GColorI.WHITE;
                }
                else {
                   color = colorizeValue(value, valueVariable.getRange());
                }
 
                //               System.out.println(point + " " + color);
                vertexContainer.addPoint(point, color);
             }
             catch (final InvalidRangeException e) {
                e.printStackTrace();
             }
             catch (final IOException e) {
                e.printStackTrace();
             }
          }
 
 
       }, ranges);
 
 
       //      System.out.println("Removed " + removedCounter.get() + " points");
 
 
       final int pointsCount = vertexContainer.size();
       //      System.out.println("pointsCount=" + pointsCount);
 
       final FloatBuffer pointsBuffer = ByteBuffer.allocateDirect(pointsCount * BYTES_PER_VECTOR3F).order(ByteOrder.nativeOrder()).asFloatBuffer();
       pointsBuffer.rewind();
       final FloatBuffer colorsBuffer = ByteBuffer.allocateDirect(pointsCount * (BYTES_PER_VECTOR3F + 4)).order(
                ByteOrder.nativeOrder()).asFloatBuffer();
       colorsBuffer.rewind();
 
       for (i = 0; i < pointsCount; i++) {
          final IVector3<?> point = vertexContainer.getPoint(i);
          pointsBuffer.put((float) point.x());
          pointsBuffer.put((float) point.y());
          pointsBuffer.put((float) point.z());
 
          final IColor color = vertexContainer.getColor(i);
          colorsBuffer.put(color.getRed());
          colorsBuffer.put(color.getGreen());
          colorsBuffer.put(color.getBlue());
          colorsBuffer.put(alpha);
       }
 
       return new IMultidimensionalData.PointsCloud(pointsBuffer, colorsBuffer);
    }
 
 
    @Override
    public IMultidimensionalData.PointsCloud calculateValuePointsCloud(final String variableName,
                                                                       final int time,
                                                                       final Globe globe,
                                                                       final double verticalExaggeration,
                                                                       final Vec4 referencePoint,
                                                                       final Map<String, GRange<Integer>> dimensionsRanges,
                                                                       final float alpha) {
 
       final ValueVariable variable = findValueVariable(variableName);
 
       return calculateValuePointsCloud(variable, time, globe, verticalExaggeration, referencePoint, dimensionsRanges, alpha);
    }
 
 
    private VectorVariable findVectorVariable(final String variableName) {
       for (final VectorVariable variable : _vectorVariables) {
          if (variable._name.equals(variableName)) {
             return variable;
          }
       }
       throw new IllegalArgumentException("Can't find vector variable named \"" + variableName + "\"");
    }
 
 
    @Override
    public IMultidimensionalData.VectorsCloud calculateVectorsCloud(final String variableName,
                                                                    final int time,
                                                                    final Globe globe,
                                                                    final double verticalExaggeration,
                                                                    final Vec4 referencePoint,
                                                                    final float factor,
                                                                    final IMultidimensionalData.VectorColorization colorization,
                                                                    final Map<String, GRange<Integer>> dimensionsRanges) {
 
 
       final VectorVariable vectorVariable = findVectorVariable(variableName);
 
 
       return calculateVectorsCloud(vectorVariable, time, globe, verticalExaggeration, referencePoint, factor, colorization,
                dimensionsRanges);
    }
 
 
    private IMultidimensionalData.VectorsCloud calculateVectorsCloud(final VectorVariable vectorVariable,
                                                                     final int time,
                                                                     final Globe globe,
                                                                     final double verticalExaggeration,
                                                                     final Vec4 referencePoint,
                                                                     final float factor,
                                                                     final IMultidimensionalData.VectorColorization colorization,
                                                                     final Map<String, GRange<Integer>> dimensionsRanges) {
 
       int initialCapacity = 1;
 
       final long start = System.currentTimeMillis();
 
       final List<Dimension> dimensions = vectorVariable._uVariable.getDimensions();
       for (final Dimension dimension : dimensions) {
          final String dimensionName = dimension.getName();
          if (!dimensionName.equals(_timeDimension.getName())) {
             initialCapacity *= dimension.getLength();
          }
       }
 
       final GVertex3Container vertexContainer = new GVertex3Container(GVectorPrecision.FLOAT, GColorPrecision.INT,
                GProjection.EUCLID, initialCapacity, false, 0, true, GColorI.WHITE, false, null);
 
       final GVertex3Container arrowVertexContainer = new GVertex3Container(GVectorPrecision.FLOAT, GColorPrecision.INT,
                GProjection.EUCLID, initialCapacity, false, 0, true, GColorI.WHITE, false, null);
 
 
       @SuppressWarnings("unchecked")
       final List<Integer>[] ranges = (List<Integer>[]) new List<?>[dimensions.size()];
 
       int i = 0;
       for (final Dimension dimension : dimensions) {
          final List<Integer> range;
          if (dimension.getName().equals(_timeDimension.getName())) {
             range = Arrays.asList(time);
          }
          else {
             //            range = GCollections.rangeList(0, dimension.getLength() - 1);
             final GRange<Integer> dimensionRange = dimensionsRanges.get(dimension.getName());
             range = GCollections.rangeList(dimensionRange._lower, dimensionRange._upper);
          }
          ranges[i++] = range;
       }
 
       //      final GHolder<Integer> removedCounter = new GHolder<Integer>(0);
 
       combination(new Processor<Integer>() {
          @Override
          public void process(final List<Integer> indices) {
             try {
 
                final String section = indices.toString().substring(1, indices.toString().length() - 1);
                final double uValue = vectorVariable._uVariable.read(section).getDouble(0);
                final double vValue = vectorVariable._vVariable.read(section).getDouble(0);
 
               if (Double.isNaN(uValue) || Double.isNaN(vValue)) {
                  return;
               }

                if (!Double.isNaN(vectorVariable._uMissingValue) && GMath.closeTo(uValue, vectorVariable._uMissingValue)) {
                   return;
                }

                if (!Double.isNaN(vectorVariable._vMissingValue) && GMath.closeTo(vValue, vectorVariable._vMissingValue)) {
                   return;
                }
 
                //               System.out.println("Vector " + uValue + "," + vValue);
 
                boolean removedPoint = false;
                final double z = get(_elevationVariable, dimensions, indices);
                if (_elevationThresholdVariable != null) {
                   final double elevationThreshold = get(_elevationThresholdVariable, dimensions, indices);
                   if (z > elevationThreshold) {
                      removedPoint = true;
                      return;
                   }
                }
 
                final double x = get(_longitudeVariable, dimensions, indices);
                final double y = get(_latitudeVariable, dimensions, indices);
                //               final double z = get(_elevationVariable, dimensions, indices);
 
                // Draw vectors centred in cell
 
                final double uValue_half_scaled = uValue * factor / 2.0;
                final double vValue_half_scaled = vValue * factor / 2.0;
 
                final Position positionCentre = new Position(Angle.fromDegrees(y), Angle.fromDegrees(x), z);
 
                final Position positionFrom = GWWUtils.increment(positionCentre, -uValue_half_scaled, -vValue_half_scaled, 0);
                final Position positionTo = GWWUtils.increment(positionCentre, uValue_half_scaled, vValue_half_scaled, 0);
 
 
                final Vec4 point4From = GWWUtils.toVec4(positionFrom, globe, verticalExaggeration);
                final Vec4 point4To = GWWUtils.toVec4(positionTo, globe, verticalExaggeration);
 
 
                final GVector3D pointFrom = new GVector3D(point4From.x - referencePoint.x, point4From.y - referencePoint.y,
                         point4From.z - referencePoint.z);
                final GVector3D pointTo = new GVector3D(point4To.x - referencePoint.x, point4To.y - referencePoint.y,
                         point4To.z - referencePoint.z);
 
                //               final GColorI color = removedPoint ? GColorI.RED : GColorI.WHITE;
                final IColor color;
                if (removedPoint) {
                   color = GColorI.RED;
                }
                else {
                   if (colorization == IMultidimensionalData.VectorColorization.RAMP_BY_ANGLE) {
                      color = colorizeVectorByAngle(uValue, vValue);
                   }
                   else {
                      color = colorization.getColor();
                   }
                }
 
                vertexContainer.addPoint(pointFrom, color);
                vertexContainer.addPoint(pointTo, color);
 
 
                //Geometry of Arrowhead
 
                final float arrowSize = factor * 0.1f;
 
                //TODO: Compute arrow-head geometry and add to vertexContainer
                //arrowVertexContainer.addPoint(pointTo.add(new GVector3D(arrowSize, arrowSize, 0.0)), color);
                //arrowVertexContainer.addPoint(pointTo.add(new GVector3D(-arrowSize, arrowSize, 0.0)), color);
 
                computeArrowheadGeometry(pointFrom, pointTo, arrowVertexContainer);
 
             }
             catch (final InvalidRangeException e) {
                e.printStackTrace();
             }
             catch (final IOException e) {
                e.printStackTrace();
             }
          }
 
 
       }, ranges);
 
 
       //      System.out.println("Removed " + removedCounter.get() + " points");
 
 
       final int pointsCount = vertexContainer.size();
       //      System.out.println("pointsCount=" + pointsCount);
 
       final FloatBuffer pointsBuffer = ByteBuffer.allocateDirect(pointsCount * BYTES_PER_VECTOR3F).order(ByteOrder.nativeOrder()).asFloatBuffer();
       pointsBuffer.rewind();
       final FloatBuffer colorsBuffer = ByteBuffer.allocateDirect(pointsCount * BYTES_PER_VECTOR3F).order(ByteOrder.nativeOrder()).asFloatBuffer();
       colorsBuffer.rewind();
       final FloatBuffer arrowsBuffer = ByteBuffer.allocateDirect((pointsCount * 3) / 2 * BYTES_PER_VECTOR3F).order(
                ByteOrder.nativeOrder()).asFloatBuffer();
       arrowsBuffer.rewind();
       final FloatBuffer arrowscolorsBuffer = ByteBuffer.allocateDirect((pointsCount * 3) / 2 * BYTES_PER_VECTOR3F).order(
                ByteOrder.nativeOrder()).asFloatBuffer();
       arrowscolorsBuffer.rewind();
 
       for (i = 0; i < pointsCount; i++) {
          final IVector3<?> point = vertexContainer.getPoint(i);
          pointsBuffer.put((float) point.x());
          pointsBuffer.put((float) point.y());
          pointsBuffer.put((float) point.z());
 
          final IColor color = vertexContainer.getColor(i);
          colorsBuffer.put(color.getRed());
          colorsBuffer.put(color.getGreen());
          colorsBuffer.put(color.getBlue());
          //         colorsBuffer.put(0.25f);
          if (i % 2 == 1) {
             final IVector3<?> pointR = arrowVertexContainer.getPoint(i - 1);
             final IVector3<?> pointL = arrowVertexContainer.getPoint(i);
 
             arrowsBuffer.put((float) point.x());
             arrowsBuffer.put((float) point.y());
             arrowsBuffer.put((float) point.z());
 
             arrowsBuffer.put((float) pointR.x());
             arrowsBuffer.put((float) pointR.y());
             arrowsBuffer.put((float) pointR.z());
 
             arrowsBuffer.put((float) pointL.x());
             arrowsBuffer.put((float) pointL.y());
             arrowsBuffer.put((float) pointL.z());
 
             for (int triPt = 0; triPt < 3; triPt++) {
                arrowscolorsBuffer.put(color.getRed());
                arrowscolorsBuffer.put(color.getGreen());
                arrowscolorsBuffer.put(color.getBlue());
             }
          }
       }
 
       if (_verbose) {
          final long elapsed = System.currentTimeMillis() - start;
          System.out.println("Vectors calculated in " + GStringUtils.getTimeMessage(elapsed));
       }
 
       return new IMultidimensionalData.VectorsCloud(pointsBuffer, colorsBuffer, arrowsBuffer, arrowscolorsBuffer);
 
 
    }
 
 
    @Override
    public String getTimeDimensionName() {
       return _timeDimension.getName();
    }
 
 
    @Override
    public List<String> getDimensionsNames() {
       return GCollections.collect(_dimensions, new ITransformer<Dimension, String>() {
          @Override
          public String transform(final Dimension dimension) {
             return dimension.getName();
          }
       });
    }
 
 
    @Override
    public List<String> getNonTimeDimensionsNames() {
       return GCollections.select(getDimensionsNames(), new IPredicate<String>() {
          @Override
          public boolean evaluate(final String dimensionName) {
             return !dimensionName.equals(_timeDimension.getName());
          }
       });
    }
 
 
    @Override
    public int getDimensionLenght(final String dimensionName) {
       return _ncFile.findDimension(dimensionName).getLength();
    }
 
 
    /**
     * Compute the geometry of a direction arrow between two points.
     * 
     * @param ptFrom
     *           Globe vector of the beginning of path
     * @param ptTo
     *           Globe vector of end of path
     * @param container
     *           Globe vertex container to place computer arrow points
     */
    protected void computeArrowheadGeometry(final GVector3D ptFrom,
                                            final GVector3D ptTo,
                                            final GVertex3Container container) {
       // Build a triangle to represent the arrowhead. The triangle is built from two vectors, one parallel to the
       // segment, and one perpendicular to it. The plane of the arrowhead will be parallel to the surface.
 
       final Vec4 ptA = new Vec4(ptFrom._x, ptFrom._y, ptFrom._z);
       final Vec4 ptB = new Vec4(ptTo._x, ptTo._y, ptTo._z);
       final double poleDistance = ptA.distanceTo3(ptB);
 
       // Compute parallel component
       Vec4 parallel = ptA.subtract3(ptB);
 
       final Vec4 surfaceNormal = new Vec4(1.0, 0.0, 0.0, 0.0);
 
       // Compute perpendicular component
       Vec4 perpendicular = surfaceNormal.cross3(parallel);
 
       // Don't draw an arrowhead if the path segment is smaller than the arrow
       if (poleDistance <= arrowLength) {
          container.addPoint(ptTo);
          container.addPoint(ptTo);
          return;
       }
 
       perpendicular = perpendicular.normalize3().multiply3(arrowBase);
       parallel = parallel.normalize3().multiply3(arrowLength);
 
 
       // Compute geometry of direction arrow
       final Vec4 vertex1 = ptB.add3(parallel).add3(perpendicular);
       final Vec4 vertex2 = ptB.add3(parallel).add3(perpendicular.multiply3(-1.0));
 
       // Add geometry to the buffer
 
       container.addPoint(new GVector3D(vertex1.x, vertex1.y, vertex1.z));
       container.addPoint(new GVector3D(vertex2.x, vertex2.y, vertex2.z));
 
    }
 
 
    /**
     * Indicates the angle of the direction arrowheads. A larger angle draws a fat arrowhead, and a smaller angle draws a narrow
     * arrow head.
     * 
     * @return The angle of the direction arrowhead tip.
     */
    public Angle getArrowAngle() {
       return this.arrowAngle;
    }
 
 
    /**
     * Indicates the length, in meters, of the direction arrowheads, from base to tip.
     * 
     * @return The geographic length of the direction arrowheads.
     */
    public double getArrowLength() {
       return this.arrowLength;
    }
 
 
    //
    //    /** {@inheritDoc} */
    //    @Override
    //    protected boolean mustRegenerateGeometry(DrawContext dc)
    //    {
    //        // Path never regenerates geometry for absolute altitude mode paths, but the direction arrows in DirectedPath
    //        // need to be recomputed because the view may have changed and the size of the arrows needs to be recalculated.
    //        if (this.getCurrentPathData().isExpired(dc))
    //            return true;
    //
    //        return super.mustRegenerateGeometry(dc);
    //    }
 
    /**
     * Determines if an direction arrow drawn a point will be less than a specified number of pixels.
     * 
     * @param dc
     *           current draw context
     * @param arrowPt
     *           point at which to draw direction arrow
     * @param numPixels
     *           the number of pixels which is considered to be "small"
     * 
     * @return {@code true} if an arrow drawn at {@code arrowPt} would occupy less than or equal to {@code numPixels}.
     */
    protected boolean isArrowheadSmall(final DrawContext dc,
                                       final Vec4 arrowPt,
                                       final int numPixels) {
       return this.getArrowLength() <= numPixels
                                       * dc.getView().computePixelSizeAtDistance(dc.getView().getEyePoint().distanceTo3(arrowPt));
    }
 
 
    //   public static void main(final String[] args) {
    //
    //      combination(new Processor<Integer>() {
    //         @Override
    //         public void process(final List<Integer> values) {
    //            System.out.println(values);
    //         }
    //      }, GCollections.rangeList(0, 0), GCollections.rangeList(10, 10));
    //   }
 
 }
