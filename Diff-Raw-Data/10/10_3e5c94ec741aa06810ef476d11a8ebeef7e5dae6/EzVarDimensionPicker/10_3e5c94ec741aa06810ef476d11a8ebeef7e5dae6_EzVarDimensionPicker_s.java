 package plugins.adufour.ezplug;
 
 import icy.sequence.DimensionId;
 import icy.sequence.Sequence;
 import plugins.adufour.vars.lang.Var;
 import plugins.adufour.vars.util.VarListener;
 
 public class EzVarDimensionPicker extends EzVarInteger
 {
     private final class SequenceChangedListener implements VarListener<Sequence>
     {
         @Override
         public void valueChanged(Var<Sequence> source, Sequence oldValue, Sequence newValue)
         {
            setValue(0);
 
             if (newValue == null)
             {
                 setVisible(false);
             }
             else
             {
                 int size = getSize(newValue, dim);
                 setVisible(size > 1);
                 setMaxValue(size - 1);
             }
         }
 
         @Override
         public void referenceChanged(Var<Sequence> source, Var<? extends Sequence> oldReference, Var<? extends Sequence> newReference)
         {
         }
     }
 
     final Var<Sequence>    s;
     final DimensionId      dim;
     final SequenceChangedListener listener;
 
     /**
      * Constructs a new selector for the specified dimension. By default, <code>-1</code> is not a
      * valid value.
      * 
      * @param varName
      *            the variable name
      * @param dim
      *            the dimension to create a selector for
      * @param sequence
      *            the sequence variable to listen to
      */
     public EzVarDimensionPicker(String varName, DimensionId dim, EzVarSequence sequence)
     {
         this(varName, dim, sequence.variable);
     }
 
     /**
      * Constructs a new selector for the specified dimension
      * 
      * @param varName
      *            the variable name
      * @param dim
      *            the dimension to create a selector for
      * @param sequence
      *            the sequence variable to listen to
      * @param allowAll
      *            true if <code>-1</code> is a valid value, i.e. all values are selected (e.g. -1
      *            for dimension C indicates that all channels should be processed). If false, the
      *            minimum selector value will be <code>0</code>
      */
     public EzVarDimensionPicker(String varName, DimensionId dim, Var<Sequence> sequence)
     {
         this(varName, dim, sequence, false);
     }
 
     /**
      * Constructs a new selector for the specified dimension
      * 
      * @param varName
      *            the variable name
      * @param dim
      *            the dimension to create a selector for
      * @param sequence
      *            the sequence variable to listen to
      * @param allowAll
      *            true if <code>-1</code> is a valid value, i.e. all values are selected (e.g. -1
      *            for dimension C indicates that all channels should be processed). If false, the
      *            minimum selector value will be <code>0</code>
      */
     public EzVarDimensionPicker(String varName, DimensionId dim, Var<Sequence> sequence, boolean allowAll)
     {
         super(varName, allowAll ? -1 : 0, allowAll ? -1 : 0, 0, 1);
         if (allowAll) setToolTipText("Choose -1 to select all values");
         s = sequence;
         this.dim = dim;
         s.addListener(listener = new SequenceChangedListener());
         listener.valueChanged(sequence, null, sequence.getValue());
     }
 
     private int getSize(Sequence s, DimensionId dim)
     {
         switch (dim)
         {
         case X:
             return s.getSizeX();
         case Y:
             return s.getSizeY();
         case Z:
             return s.getSizeZ();
         case T:
             return s.getSizeT();
         case C:
             return s.getSizeC();
         default:
             throw new IllegalArgumentException("Unknown sequence dimension: " + dim);
         }
     }
 
     @Override
     protected void dispose()
     {
         s.removeListener(listener);
         super.dispose();
     }
 }
