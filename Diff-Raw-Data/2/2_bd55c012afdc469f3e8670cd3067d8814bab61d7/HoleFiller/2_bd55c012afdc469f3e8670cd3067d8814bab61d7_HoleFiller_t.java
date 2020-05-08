 package plugins.adufour.activecontours;
 
 import icy.roi.ROI2DArea;
 import icy.sequence.Sequence;
 import icy.sequence.SequenceUtil;
 import icy.type.DataType;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzVarSequence;
 
 public class HoleFiller extends EzPlug
 {
     EzVarSequence in;
     
     @Override
     public void initialize()
     {
         addEzComponent(in = new EzVarSequence("Input"));
         super.setTimeDisplay(true);
     }
     
     @Override
     public void execute()
     {
         Sequence out = SequenceUtil.convertToType(SequenceUtil.getCopy(in.getValue(true)), DataType.UBYTE, true);
         
         long tic = System.nanoTime();
         
         fillHoles2D(out, (byte) 0, (byte) 255);
         
         long tac = System.nanoTime();
         
         System.out.println((tac - tic) / 1000000 + " ms");
         addSequence(out);
     }
     
     public void fillHoles2D(ROI2DArea bin)
     {
         int width = bin.getBounds().width;
         int height = bin.getBounds().height;
         int slice = width * height;
         
        boolean[] in = bin.getBooleanMask(bin.getBounds());
         boolean bgValue = false, fillValue = true;
         
         final byte[] pixels = new byte[in.length];
         final byte FREE = 0;
         final byte BACKGROUND = 1;
         final byte LOCKED = 2;
         
         int[] neighbors = new int[in.length];
         int n = 0;
         
         // 1) extract background pixels on the image edges
         
         for (int top = 0, bottom = slice - width; top < width; top++, bottom++)
         {
             
             if (in[top] == bgValue)
             {
                 pixels[top] = BACKGROUND;
                 neighbors[n++] = top + width;
             }
             if (in[bottom] == bgValue)
             {
                 pixels[bottom] = BACKGROUND;
                 neighbors[n++] = bottom - width;
             }
         }
         
         for (int left = 1, right = width - 1; left < slice; left += width, right += width)
         {
             if (in[left] == bgValue)
             {
                 pixels[left] = BACKGROUND;
                 neighbors[n++] = left + 1;
             }
             if (in[right] == bgValue)
             {
                 pixels[right] = BACKGROUND;
                 neighbors[n++] = right - 1;
             }
         }
         
         if (n == 0) System.err.println("fillHoles_2D was unable to find a background pixel for flooding");
         
         // 2) flood the image from the list of border pixels
         
         for (int index = 0; index < n; index++)
         {
             int offset = neighbors[index];
             
             if (pixels[offset] != BACKGROUND && in[offset] == bgValue)
             {
                 pixels[offset] = BACKGROUND;
                 
                 if (pixels[offset - 1] == FREE)
                 {
                     neighbors[n++] = offset - 1;
                     pixels[offset - 1] = LOCKED;
                 }
                 
                 if (pixels[offset + 1] == FREE)
                 {
                     neighbors[n++] = offset + 1;
                     pixels[offset + 1] = LOCKED;
                 }
                 
                 if (pixels[offset - width] == FREE)
                 {
                     neighbors[n++] = offset - width;
                     pixels[offset - width] = LOCKED;
                 }
                 
                 if (pixels[offset + width] == FREE)
                 {
                     neighbors[n++] = offset + width;
                     pixels[offset + width] = LOCKED;
                 }
             }
         }
         
         for (int i = 0; i < slice; i++)
             if (pixels[i] == FREE && in[i] == bgValue) in[i] = fillValue;
         
         bin.setAsBooleanMask(bin.getBounds(), in);
     }
     
     public void fillHoles2D(Sequence bin, byte bgValue, byte fillValue)
     {
         int width = bin.getWidth();
         int height = bin.getHeight();
         int slice = width * height;
         
         byte[] in = bin.getDataXYAsByte(0, 0, 0);
         
         final byte[] pixels = new byte[in.length];
         final byte FREE = 0;
         final byte BACKGROUND = 1;
         final byte LOCKED = 2;
         
         int[] neighbors = new int[in.length];
         int n = 0;
         
         // 1) extract background pixels on the image edges
         
         for (int top = 0, bottom = slice - width; top < width; top++, bottom++)
         {
             
             if (in[top] == bgValue)
             {
                 pixels[top] = BACKGROUND;
                 neighbors[n++] = top + width;
             }
             if (in[bottom] == bgValue)
             {
                 pixels[bottom] = BACKGROUND;
                 neighbors[n++] = bottom - width;
             }
         }
         
         for (int left = 1, right = width - 1; left < slice; left += width, right += width)
         {
             if (in[left] == bgValue)
             {
                 pixels[left] = BACKGROUND;
                 neighbors[n++] = left + 1;
             }
             if (in[right] == bgValue)
             {
                 pixels[right] = BACKGROUND;
                 neighbors[n++] = right - 1;
             }
         }
         
         if (n == 0) System.err.println("fillHoles_2D was unable to find a background pixel for flooding");
         
         // 2) flood the image from the list of border pixels
         
         for (int index = 0; index < n; index++)
         {
             int offset = neighbors[index];
             
             if (pixels[offset] != BACKGROUND && in[offset] == bgValue)
             {
                 pixels[offset] = BACKGROUND;
                 
                 if (pixels[offset - 1] == FREE)
                 {
                     neighbors[n++] = offset - 1;
                     pixels[offset - 1] = LOCKED;
                 }
                 
                 if (pixels[offset + 1] == FREE)
                 {
                     neighbors[n++] = offset + 1;
                     pixels[offset + 1] = LOCKED;
                 }
                 
                 if (pixels[offset - width] == FREE)
                 {
                     neighbors[n++] = offset - width;
                     pixels[offset - width] = LOCKED;
                 }
                 
                 if (pixels[offset + width] == FREE)
                 {
                     neighbors[n++] = offset + width;
                     pixels[offset + width] = LOCKED;
                 }
             }
         }
         
         for (int i = 0; i < slice; i++)
             if (pixels[i] == FREE && in[i] == bgValue) in[i] = fillValue;
     }
     
     public void clean()
     {
     }
     
 }
