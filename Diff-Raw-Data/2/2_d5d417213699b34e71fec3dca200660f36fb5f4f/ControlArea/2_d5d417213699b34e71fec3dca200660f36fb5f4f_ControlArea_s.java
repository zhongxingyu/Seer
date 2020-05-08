package pdc.de.psmcsv;
 
 public class ControlArea extends PsrNamedObject
 {
 	Float _frequencyBias;
 	String _description;
 	
 	public Float getFrequencyBias() {return _frequencyBias;}
 	public String getDescription() {return _description;}
 	
 	@Override
 	public void configure(RecordReader rr)
 	{
 		_frequencyBias = processFloat(rr, "frequencybias");
 		_description = rr.getProperty("description");
 		super.configure(rr);
 	}
 
 	@Override
 	public String toString()
 	{
 		StringBuilder sb = super.reportString();
 		sb.append(", frequencyBias=");
 		sb.append(_frequencyBias);
 		sb.append(", description=");
 		sb.append(_description);
 		return sb.toString();
 	}
 
 	
 }
