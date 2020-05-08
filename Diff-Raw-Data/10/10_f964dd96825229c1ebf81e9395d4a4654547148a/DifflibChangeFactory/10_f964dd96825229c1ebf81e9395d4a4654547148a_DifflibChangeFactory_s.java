 package witchdoctor.detect.lldiff;
 
 import witchdoctor.WitchDoctorException;
 import witchdoctor.detect.changes.IChangeFactory;
 import witchdoctor.detect.changes.IMacroChange;
 import difflib.Chunk;
 import difflib.Delta;
 import difflib.Delta.TYPE;
 
 public class DifflibChangeFactory {
 	
 	@SuppressWarnings("unchecked")
 	public IMacroChange<Object> create(Delta delta, IChangeFactory factory) 
 	throws WitchDoctorException {
		if (delta.getType() == TYPE.CHANGE)
			throw new WitchDoctorException();
 		boolean isdeletion = delta.getType() == TYPE.DELETE;
 		Chunk content = isdeletion ? delta.getOriginal() : delta.getRevised();
 		return factory.createMacro(
 			isdeletion, 
 			content.getPosition(), 
 			(Iterable<Object>)content.getLines());
 	}
 
 }
