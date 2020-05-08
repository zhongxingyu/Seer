 package plugins.adufour.filtering;
 
 import icy.sequence.Sequence;
 import icy.system.SystemUtil;
 import icy.type.DataType;
 import icy.type.collection.array.Array1DUtil;
 
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import plugins.adufour.filtering.FilterToolbox.Axis;
 import plugins.adufour.vars.lang.VarBoolean;
 
 /**
  * 
  * Spatial convolution for separable 1D kernels.
  * 
  * @author Alexandre Dufour
  * 
  */
 public class Convolution1D
 {
     /**
      * Spatial convolution for separable kernels. <br>
      * This method accept kernels as 1D sequences which can either have: <br>
      * - a single time point and channel (applied to the entire sequence) <br>
      * - one kernel per time point and channel (applied individually)
      * 
      * @param sequence
      *            the Sequence to convolve
      * @param kernel1D_X
      *            the kernel to use for convolution along X
      * @param kernel1D_Y
      *            the kernel to use for convolution along Y
      * @param kernel1D_Z
      *            the kernel to use for convolution along Z
      * @throws ConvolutionException
      *             if a kernel is too large w.r.t. the image size
      */
     public static void convolve(Sequence sequence, Sequence kernel1D_X, Sequence kernel1D_Y, Sequence kernel1D_Z) throws ConvolutionException
     {
         convolve(sequence, kernel1D_X, kernel1D_Y, kernel1D_Z, 1, new VarBoolean("stop", false));
     }
     
     /**
      * Spatial convolution for separable kernels. <br>
      * This method accept kernels as 1D sequences which can either have: <br>
      * - a single time point and channel (applied to the entire sequence) <br>
      * - one kernel per time point and channel (applied individually)
      * 
      * @param sequence
      *            the Sequence to convolve
      * @param kernel1D_X
      *            the kernel to use for convolution along X
      * @param kernel1D_Y
      *            the kernel to use for convolution along Y
      * @param kernel1D_Z
      *            the kernel to use for convolution along Z
      * @throws ConvolutionException
      *             if a kernel is too large w.r.t. the image size
      */
     public static void convolve(Sequence sequence, Sequence kernel1D_X, Sequence kernel1D_Y, Sequence kernel1D_Z, int nbIter, VarBoolean stopFlag) throws ConvolutionException
     {
         if (kernel1D_X == null && kernel1D_Y == null && kernel1D_Z == null) throw new IllegalArgumentException("Invalid argument: provide at least one non-null kernel");
         
         if (kernel1D_X != null)
         {
             if (kernel1D_X.getSizeY() > 1 || kernel1D_X.getSizeZ() > 1) throw new IllegalArgumentException("kernel along X is not 1D");
             if (kernel1D_X.getSizeX() % 2 == 0) throw new IllegalArgumentException("kernel along X has even size");
             if (kernel1D_X.getSizeC() != 1 && kernel1D_X.getSizeC() != sequence.getSizeC()) throw new IllegalArgumentException("kernel along X has " + kernel1D_X.getSizeC() + " channels");
         }
         if (kernel1D_Y != null)
         {
             if (kernel1D_Y.getSizeY() > 1 || kernel1D_Y.getSizeZ() > 1) throw new IllegalArgumentException("kernel along Y is not 1D");
             if (kernel1D_Y.getSizeX() % 2 == 0) throw new IllegalArgumentException("kernel along Y has even size");
             if (kernel1D_Y.getSizeC() != 1 && kernel1D_Y.getSizeC() != sequence.getSizeC()) throw new IllegalArgumentException("kernel along Y has " + kernel1D_Y.getSizeC() + " channels");
         }
         if (kernel1D_Z != null)
         {
             if (kernel1D_Z.getSizeY() > 1 || kernel1D_Z.getSizeZ() > 1) throw new IllegalArgumentException("kernel along Z is not 1D");
             if (kernel1D_Z.getSizeX() % 2 == 0) throw new IllegalArgumentException("kernel along Z has even size");
             if (kernel1D_Z.getSizeC() != 1 && kernel1D_Z.getSizeC() != sequence.getSizeC()) throw new IllegalArgumentException("kernel along Z has " + kernel1D_Z.getSizeC() + " channels");
         }
         
         double[] kernelX = null;
         double[] kernelY = null;
         double[] kernelZ = null;
         
         sequence.beginUpdate();
         
         // Special case: if the input data is already of type double, no conversion is needed.
         // => use shortcut methods to perform direct "in-place" convolution
         
         DataType type = sequence.getDataType_();
         
         if (type == DataType.DOUBLE)
         {
             convolution: for (int t = 0; t < sequence.getSizeT(); t++)
                 for (int c = 0; c < sequence.getSizeC(); c++)
                 {
                     if (kernel1D_X != null) kernelX = kernel1D_X.getDataXYAsDouble(Math.min(t, kernel1D_X.getSizeT() - 1), 0, Math.min(c, kernel1D_X.getSizeC() - 1));
                     if (kernel1D_Y != null) kernelY = kernel1D_Y.getDataXYAsDouble(Math.min(t, kernel1D_Y.getSizeT() - 1), 0, Math.min(c, kernel1D_Y.getSizeC() - 1));
                     if (kernel1D_Z != null) kernelZ = kernel1D_Z.getDataXYAsDouble(Math.min(t, kernel1D_Z.getSizeT() - 1), 0, Math.min(c, kernel1D_Z.getSizeC() - 1));
                     
                     for (int i = 0; i < nbIter; i++)
                     {
                         convolve(sequence.getDataXYZAsDouble(t, c), sequence.getSizeX(), sequence.getSizeY(), kernelX, kernelY, kernelZ);
                         
                         if (stopFlag.getValue()) break convolution;
                     }
                 }
         }
         else
         {
             double[][] z_xy = new double[sequence.getSizeZ()][sequence.getSizeX() * sequence.getSizeY()];
             
             convolution: for (int t = 0; t < sequence.getSizeT(); t++)
                 for (int c = 0; c < sequence.getSizeC(); c++)
                 {
                     if (kernel1D_X != null) kernelX = kernel1D_X.getDataXYAsDouble(Math.min(t, kernel1D_X.getSizeT() - 1), 0, Math.min(c, kernel1D_X.getSizeC() - 1));
                     if (kernel1D_Y != null) kernelY = kernel1D_Y.getDataXYAsDouble(Math.min(t, kernel1D_Y.getSizeT() - 1), 0, Math.min(c, kernel1D_Y.getSizeC() - 1));
                     if (kernel1D_Z != null) kernelZ = kernel1D_Z.getDataXYAsDouble(Math.min(t, kernel1D_Z.getSizeT() - 1), 0, Math.min(c, kernel1D_Z.getSizeC() - 1));
                     
                     for (int i = 0; i < nbIter; i++)
                     {
                         for (int z = 0; z < sequence.getSizeZ(); z++)
                             Array1DUtil.arrayToDoubleArray(sequence.getDataXY(t, z, c), z_xy[z], type.isSigned());
                         
                         convolve(z_xy, sequence.getSizeX(), sequence.getSizeY(), kernelX, kernelY, kernelZ);
                         
                         for (int z = 0; z < sequence.getSizeZ(); z++)
                         {
                             // ArrayMath.rescale(z_xy[z], sequence.getComponentMinValue(c),
                             // sequence.getComponentMaxValue(c), true);
                             Array1DUtil.doubleArrayToSafeArray(z_xy[z], sequence.getDataXY(t, z, c), type.isSigned());
                         }
                         
                         if (stopFlag.getValue()) break convolution;
                     }
                 }
         }
         sequence.endUpdate();
     }
     
     /**
      * Spatial convolution for separable kernels. The final convolution result is obtained by
      * sequentially convolving along each direction using a 1D kernel
      * 
      * @param sequence
      *            the Sequence to convolve
      * @param kernelX
      *            the kernel to use for convolution along X
      * @param kernelY
      *            the kernel to use for convolution along Y
      * @param kernelZ
      *            the kernel to use for convolution along Z
      * @throws IllegalArgumentException
      *             if all kernels are null or if a kernel has even size
      * @throws ConvolutionException
      *             if a kernel is too large w.r.t. the image size
      */
     public static void convolve(Sequence sequence, double[] kernelX, double[] kernelY, double[] kernelZ) throws IllegalArgumentException, ConvolutionException
     {
         if (kernelX == null && kernelY == null && kernelZ == null) throw new IllegalArgumentException("Invalid argument: provide at least one non-null kernel");
         if (kernelX != null && kernelX.length % 2 == 0) throw new IllegalArgumentException("Invalid argument: kernel along X has even size");
         if (kernelY != null && kernelY.length % 2 == 0) throw new IllegalArgumentException("Invalid argument: kernel along Y has even size");
         if (kernelZ != null && kernelZ.length % 2 == 0) throw new IllegalArgumentException("Invalid argument: kernel along Z has even size");
         
         // Special case: if the input data is already of type double, no conversion is needed.
         // => use shortcut methods to perform direct "in-place" convolution
         
         DataType type = sequence.getDataType_();
         
         if (type == DataType.DOUBLE)
         {
             for (int t = 0; t < sequence.getSizeT(); t++)
                 for (int c = 0; c < sequence.getSizeC(); c++)
                     convolve(sequence.getDataXYZAsDouble(t, c), sequence.getSizeX(), sequence.getSizeY(), kernelX, kernelY, kernelZ);
         }
         else
         {
             double[][] z_xy = new double[sequence.getSizeZ()][sequence.getSizeX() * sequence.getSizeY()];
             
             for (int t = 0; t < sequence.getSizeT(); t++)
                 for (int c = 0; c < sequence.getSizeC(); c++)
                 {
                     for (int z = 0; z < sequence.getSizeZ(); z++)
                         Array1DUtil.arrayToDoubleArray(sequence.getDataXY(t, z, c), z_xy[z], type.isSigned());
                     
                     convolve(z_xy, sequence.getSizeX(), sequence.getSizeY(), kernelX, kernelY, kernelZ);
                     
                     for (int z = 0; z < sequence.getSizeZ(); z++)
                     {
                         Array1DUtil.doubleArrayToSafeArray(z_xy[z], sequence.getDataXY(t, z, c), type.isSigned());
                     }
                 }
         }
     }
     
     /**
      * Low-level 3D separable convolution. <br>
      * The convolution is made "in-place", i.e. the input array is overwritten upon return. <br>
      * Warning: this is a low-level method. No check is performed on the input arguments, and the
      * method may return successfully though with incorrect results. Make sure your arguments follow
      * the indicated constraints.
      * 
      * @param input_Z_XY
      *            the input data buffer, given as a [Z (slice)][XY (1D offset)] double array
      * @param output_Z_XY
      *            the output data buffer, given as a [Z (slice)][XY (1D offset)] double array.
      * @param imageWidth
      *            the image width
      * @param imageHeight
      *            the image height
      * @param kernelX
      *            a 1D odd-length kernel to convolve along X (or null to skip convolution along X)
      * @param kernelY
      *            a 1D odd-length kernel to convolve along Y (or null to skip convolution along Y)
      * @param kernelZ
      *            a 1D odd-length kernel to convolve along Z (or null to skip convolution along Z)
      * @throws ConvolutionException
      *             if a kernel is too large w.r.t the image size
      */
     public static void convolve(double[][] array, int imageWidth, int imageHeight, double[] kernelX, double[] kernelY, double[] kernelZ) throws ConvolutionException
     {
         ExecutorService service = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
         
         int sliceSize = array[0].length;
         
         double[][] temp = new double[array.length][sliceSize];
         
         if (array.length == 1)
         {
             if (kernelX == null)
             {
                 convolve1D(service, array, temp, imageWidth, imageHeight, kernelY, Axis.Y);
                 
                 for (int z = 0; z < array.length; z++)
                     System.arraycopy(temp[z], 0, array[z], 0, sliceSize);
             }
             else if (kernelY == null)
             {
                 convolve1D(service, array, temp, imageWidth, imageHeight, kernelX, Axis.X);
                 
                 for (int z = 0; z < array.length; z++)
                     System.arraycopy(temp[z], 0, array[z], 0, sliceSize);
             }
             else
             {
                 convolve1D(service, array, temp, imageWidth, imageHeight, kernelX, Axis.X);
                 convolve1D(service, temp, array, imageWidth, imageHeight, kernelY, Axis.Y);
             }
         }
         else
         {
             if (kernelX == null)
             {
                 if (kernelY == null)
                 {
                     convolve1D(service, array, temp, imageWidth, imageHeight, kernelZ, Axis.Z);
                     
                     for (int z = 0; z < array.length; z++)
                         System.arraycopy(temp[z], 0, array[z], 0, sliceSize);
                 }
                 else if (kernelZ == null)
                 {
                     convolve1D(service, array, temp, imageWidth, imageHeight, kernelY, Axis.Y);
                     
                     for (int z = 0; z < array.length; z++)
                         System.arraycopy(temp[z], 0, array[z], 0, sliceSize);
                 }
                 else
                 {
                     convolve1D(service, array, temp, imageWidth, imageHeight, kernelY, Axis.Y);
                     convolve1D(service, temp, array, imageWidth, imageHeight, kernelZ, Axis.Z);
                 }
             }
             // kernel_X is not null from here on
             else if (kernelY == null)
             {
                 if (kernelZ == null)
                 {
                     convolve1D(service, array, temp, imageWidth, imageHeight, kernelX, Axis.X);
                     
                     for (int z = 0; z < array.length; z++)
                         System.arraycopy(temp[z], 0, array[z], 0, sliceSize);
                 }
                 else
                 {
                     convolve1D(service, array, temp, imageWidth, imageHeight, kernelX, Axis.X);
                     convolve1D(service, temp, array, imageWidth, imageHeight, kernelZ, Axis.Z);
                 }
             }
             // kernel_X and kernel_Y are not null from here
             else if (kernelZ == null)
             {
                 convolve1D(service, array, temp, imageWidth, imageHeight, kernelX, Axis.X);
                 convolve1D(service, temp, array, imageWidth, imageHeight, kernelY, Axis.Y);
             }
             else
             {
                 convolve1D(service, array, temp, imageWidth, imageHeight, kernelX, Axis.X);
                 convolve1D(service, temp, array, imageWidth, imageHeight, kernelY, Axis.Y);
                 convolve1D(service, array, temp, imageWidth, imageHeight, kernelZ, Axis.Z);
                 
                 for (int z = 0; z < array.length; z++)
                     System.arraycopy(temp[z], 0, array[z], 0, sliceSize);
             }
         }
        
        service.shutdown();
     }
     
     /**
      * Low-level 1D convolution method. <br>
      * Warning: this is a low-level method. No check is performed on the input arguments, and the
      * method may return successfully though with incorrect results. Make sure your arguments follow
      * the indicated constraints.
      * 
      * @param input
      *            the input image data buffer, given as a [Z (slice)][XY (1D offset)] double array
      * @param output
      *            the output image data buffer, given as a [Z (slice)][XY (1D offset)] double array
      *            (must point to a different array than the input)
      * @param width
      *            the image width
      * @param height
      *            the image height
      * @param kernel
      *            an odd-length convolution kernel
      * @param axis
      *            the axis along which to convolve
      * @throws ConvolutionException
      *             if a kernel is too large w.r.t. the image size
      */
     public static void convolve1D(double[][] input, double[][] output, int width, int height, double[] kernel, Axis axis) throws ConvolutionException
     {
         ExecutorService service = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
         convolve1D(service, input, output, width, height, kernel, axis);
         service.shutdown();
     }
     
     /**
      * Low-level 1D convolution method. <br>
      * Warning: this is a low-level method. No check is performed on the input arguments, and the
      * method may return successfully though with incorrect results. Make sure your arguments follow
      * the indicated constraints.
      * 
      * @param input
      *            the input image data buffer, given as a [Z (slice)][XY (1D offset)] double array
      * @param output
      *            the output image data buffer, given as a [Z (slice)][XY (1D offset)] double array
      *            (must point to a different array than the input)
      * @param width
      *            the image width
      * @param height
      *            the image height
      * @param kernel
      *            an odd-length convolution kernel
      * @param axis
      *            the axis along which to convolve
      * @throws ConvolutionException
      *             if a kernel is too large w.r.t. the image size
      */
     public static void convolve1D(ExecutorService service, final double[][] input, final double[][] output, final int width, final int height, final double[] kernel, Axis axis)
             throws ConvolutionException
     {
         try
         {
             final int sliceSize = input[0].length;
             
             final int kRadius = (kernel.length - 1) / 2;
             
             switch (axis)
             {
                 case X:
                 {
                     ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(input.length);
                     
                     for (int z = 0; z < input.length; z++)
                     {
                         final double[] inSlice = input[z];
                         final double[] outSlice = output[z];
                         
                         tasks.add(service.submit(new Runnable()
                         {
                             public void run()
                             {
                                 int xy = 0;
                                 for (int y = 0; y < height; y++)
                                 {
                                     int x = 0;
                                     
                                     // store the offset of the first and last elements of the line
                                     // they will be used to compute mirror conditions
                                     int xStartOffset = xy;
                                     int xEndOffset = xy + width - 1;
                                     
                                     // convolve the west border (mirror condition)
                                     
                                     for (; x < kRadius; x++, xy++)
                                     {
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadius; kOffset <= kRadius; kOffset++, kIndex++)
                                         {
                                             int inOffset = xy + kOffset;
                                             if (inOffset < xStartOffset) inOffset = xStartOffset + (xStartOffset - inOffset);
                                             
                                             value += inSlice[inOffset] * kernel[kIndex];
                                         }
                                         
                                         outSlice[xy] = value;
                                     }
                                     
                                     // convolve the central area until the east border
                                     
                                     int eastBorder = width - kRadius;
                                     
                                     for (; x < eastBorder; x++, xy++)
                                     {
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadius; kOffset <= kRadius; kOffset++, kIndex++)
                                         {
                                             value += inSlice[xy + kOffset] * kernel[kIndex];
                                         }
                                         
                                         outSlice[xy] = value;
                                     }
                                     
                                     // convolve the east border
                                     
                                     for (; x < width; x++, xy++)
                                     {
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadius; kOffset <= kRadius; kOffset++, kIndex++)
                                         {
                                             int inOffset = xy + kOffset;
                                             if (inOffset >= xEndOffset) inOffset = xEndOffset - (inOffset - xEndOffset);
                                             
                                             value += inSlice[inOffset] * kernel[kIndex];
                                         }
                                         
                                         outSlice[xy] = value;
                                     }
                                 }
                             }
                         }));
                     }
                     
                     for (Future<?> task : tasks)
                         task.get();
                 }
                 break;
                 
                 case Y:
                 {
                     final int kRadiusY = kRadius * width;
                     
                     ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(input.length);
                     
                     for (int z = 0; z < input.length; z++)
                     {
                         final double[] in = input[z];
                         final double[] out = output[z];
                         
                         tasks.add(service.submit(new Runnable()
                         {
                             public void run()
                             {
                                 
                                 int xy = 0;
                                 
                                 int y = 0;
                                 
                                 // convolve the north border (mirror condition)
                                 
                                 for (; y < kRadius; y++)
                                 {
                                     for (int x = 0; x < width; x++, xy++)
                                     {
                                         int yStartOffset = x;
                                         
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadiusY; kOffset <= kRadiusY; kOffset += width, kIndex++)
                                         {
                                             int inOffset = xy + kOffset;
                                             if (inOffset < 0) inOffset = yStartOffset - inOffset;
                                             
                                             value += in[inOffset] * kernel[kIndex];
                                         }
                                         
                                         out[xy] = value;
                                     }
                                 }
                                 
                                 // convolve the central area until the south border
                                 
                                 int southBorder = height - kRadius;
                                 
                                 for (; y < southBorder; y++)
                                 {
                                     for (int x = 0; x < width; x++, xy++)
                                     {
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadiusY; kOffset <= kRadiusY; kOffset += width, kIndex++)
                                         {
                                             value += in[xy + kOffset] * kernel[kIndex];
                                         }
                                         
                                         out[xy] = value;
                                     }
                                 }
                                 
                                 // convolve the south border
                                 
                                 for (; y < height; y++)
                                 {
                                     for (int x = 0; x < width; x++, xy++)
                                     {
                                         int yEndOffset = sliceSize - width + x;
                                         
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadiusY; kOffset <= kRadiusY; kOffset += width, kIndex++)
                                         {
                                             int inOffset = xy + kOffset;
                                             if (inOffset >= sliceSize) inOffset = yEndOffset - (inOffset - yEndOffset);
                                             
                                             value += in[inOffset] * kernel[kIndex];
                                         }
                                         
                                         out[xy] = value;
                                     }
                                 }
                             }
                         }));
                     }
                     for (Future<?> task : tasks)
                         task.get();
                 }
                 break;
                 
                 case Z:
                 {
                     ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(input.length);
                     
                     int z = 0;
                     for (; z < kRadius; z++)
                     {
                         final double[] out = output[z];
                         final int slice = z;
                         tasks.add(service.submit(new Runnable()
                         {
                             public void run()
                             {
                                 
                                 int xy = 0;
                                 
                                 for (int y = 0; y < height; y++)
                                 {
                                     for (int x = 0; x < width; x++, xy++)
                                     {
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadius; kOffset <= kRadius; kOffset++, kIndex++)
                                         {
                                             int inSlice = slice + kOffset;
                                             if (inSlice < 0) inSlice = -inSlice;
                                             
                                             value += input[inSlice][xy] * kernel[kIndex];
                                         }
                                         
                                         out[xy] = value;
                                     }
                                 }
                             }
                         }));
                     }
                     
                     int bottomBorder = input.length - kRadius;
                     
                     for (; z < bottomBorder; z++)
                     {
                         final double[] out = output[z];
                         final int slice = z;
                         tasks.add(service.submit(new Runnable()
                         {
                             public void run()
                             {
                                 
                                 int xy = 0;
                                 
                                 for (int y = 0; y < height; y++)
                                 {
                                     for (int x = 0; x < width; x++, xy++)
                                     {
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadius; kOffset <= kRadius; kOffset++, kIndex++)
                                         {
                                             value += input[slice + kOffset][xy] * kernel[kIndex];
                                         }
                                         
                                         out[xy] = value;
                                     }
                                 }
                             }
                         }));
                     }
                     
                     final int zEndOffset = input.length - 1;
                     
                     for (; z < input.length; z++)
                     {
                         final double[] out = output[z];
                         final int slice = z;
                         tasks.add(service.submit(new Runnable()
                         {
                             public void run()
                             {
                                 int xy = 0;
                                 
                                 for (int y = 0; y < height; y++)
                                 {
                                     for (int x = 0; x < width; x++, xy++)
                                     {
                                         double value = 0;
                                         
                                         for (int kIndex = 0, kOffset = -kRadius; kOffset <= kRadius; kOffset++, kIndex++)
                                         {
                                             int inSlice = slice + kOffset;
                                             if (inSlice >= input.length) inSlice = zEndOffset - (inSlice - zEndOffset);
                                             
                                             value += input[inSlice][xy] * kernel[kIndex];
                                         }
                                         
                                         out[xy] = value;
                                     }
                                 }
                             }
                         }));
                     }
                     
                     for (Future<?> task : tasks)
                         task.get();
                 }
                 break;
             }
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
             throw new ConvolutionException("Filter size is too large along " + axis.name(), e);
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
     }
     
 }
