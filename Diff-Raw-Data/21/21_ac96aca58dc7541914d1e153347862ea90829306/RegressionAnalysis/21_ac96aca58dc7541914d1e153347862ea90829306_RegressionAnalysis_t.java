 package exsys.factor;
 import java.lang.reflect.Field;
 
 import org.apache.commons.math3.stat.regression.SimpleRegression;
 import org.apache.commons.math3.util.ArithmeticUtils;
 
 import exsys.data.Tuple;
 
 /* 2.2.3 Regression Analysis and Field Correlation Checks
  * 
  * The fields of the dataset may be calculated for Pearson product-moment
  * correlation coefficients. These coefficients will be used to check
  * incoming tuples independent of the data set. If the field correlation
  * coefficients do not agree with that of the data set then it shows
  * erroneous behavior in measurement. Correlations may not exist or be
  * strong enough to determine correctness of new data points.
  */
 
 public class RegressionAnalysis implements QualityFactor
 {
 	private static double PPMCC_STRONG_BOUNDRY  = 0.9;
	private static double ERROR_DILUTION_FACTOR = 0.625;
	private static double FLYWHEEL_ENABLE_WAIT  = 128;
 	
 	private SimpleRegression[] regressions_;
 	
 	private Field[] fields_;
 	private int     length_;
 	
 	private int combinations_ = -1;
 	
	private int count_ = 0;
	
 	public RegressionAnalysis()
 	{
 		fields_ = Tuple.class.getFields();
 		length_ = fields_.length;
 		
 		combinations_ = (int) ArithmeticUtils.binomialCoefficient(length_, 2);
 		
 		regressions_  = new SimpleRegression[combinations_];
 		
 		for(int i = 0; i < combinations_; i++)
 			regressions_[i] = new SimpleRegression();
 	}
 	
 	@Override
 	public float run(Tuple tuple)
 	{	
 		double confidence = 1.0;
 		try
 		{
 			double[] values = parseValues(tuple);
 			int i = 0;
 			int c = 0;
 			
 			for(int a = 0; a < length_; a++)
 			{
 				for(int b = a+1; b < length_; b++)
 				{
 					double conf = regress_field_combination(a,b, values, regressions_[i++]);
 					if(!Double.isNaN(conf))
 					{
 						confidence *= conf;
 					}
 				}
 			}
			count_++;
 		}
 		catch (IllegalArgumentException | IllegalAccessException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
		if(count_ < FLYWHEEL_ENABLE_WAIT)
		{
			confidence = 1.0;
		}
 		return (float) confidence;
 	}
    
 	private double regress_field_combination(int a, int b, double[] values, SimpleRegression reg)
 	{
 		double conf = Double.NaN;
		double r = reg.getR();
		if(Math.abs(r) > PPMCC_STRONG_BOUNDRY && !Double.isNaN(r))
 		{
 			conf = detect_prediction_errors(values[a], values[b], reg);
 		}
 		
 		reg.addData( values[a], values[b] );
 		
 		return conf;
 	}
 
 	private double detect_prediction_errors(double a, double b, SimpleRegression reg)
 	{	
 		double b_hat = reg.predict(a);
		double error = (Math.abs(b_hat - b) / Math.abs(b)) * ERROR_DILUTION_FACTOR;
 		
 		if(error > 1.0)
 		{
 			error = 1.0;
 		}
 		
 		return 1 - error;
 	}
 
 	private double[] parseValues(Tuple tuple) throws IllegalArgumentException, IllegalAccessException
 	{
 		double[] values = new double[length_];
 		for(int i = 0; i < length_; i++)
 		{
 			values[i] = fields_[i].getDouble(tuple);
 		}
 		
 		return values;
 	}
 }
