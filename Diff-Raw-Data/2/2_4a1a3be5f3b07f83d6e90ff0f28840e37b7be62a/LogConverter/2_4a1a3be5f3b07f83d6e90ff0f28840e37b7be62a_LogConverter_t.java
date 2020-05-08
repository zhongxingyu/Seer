 package org.ngs.ngunits.converter;
 
 import org.ngs.ngunits.UnitConverter;
 
 
 /**
  * <p> This class represents a exponential converter of limited precision.
  *     Such converter  is typically used to create inverse of logarithmic unit.
  *
  * <p> Instances of this class are immutable.</p>
  */
 public abstract class LogConverter extends AbstractUnitConverter 
 {
     protected final double _base;
 
     protected final double _logOfBase;
 
     protected LogConverter (double base) {
         _base = base;
         _logOfBase = Math.log(base);
     }
     
     public double getBase() {
         return _base;
     }
     
     @Override
     public boolean isLinear () {
         return false;
     }
     
     public static final class Log extends LogConverter 
     {
         public Log (double base) {
             super(base);
         }
 
         public UnitConverter inverse () {
             return new Exp(_base);
         }
 
         public double convert (double value) {
             return Math.log(value) / _logOfBase;
         }
     }
     
     public static final class Exp extends LogConverter
     {
         public Exp (double base) {
             super(base);
         }
 
         public UnitConverter inverse () {
             return new Log(_base);
         }
         
         public double convert (double value) {
            return Math.exp(value * _logOfBase);
         }
     }
 }
