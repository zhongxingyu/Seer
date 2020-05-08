 package individual;
 
 import genotype.Genotype;
 import zone.Zone;
 
 public class MultiProcObjectPull extends IndividualsManager {
 
 	private ObjectPull[] objectPulls;
 	private int curSubStorage=0;
 	
 	public MultiProcObjectPull(int numberOfFragmentsOfStorage){
 		if (numberOfFragmentsOfStorage <= 1)
 			objectPulls = new ObjectPull[]{new ObjectPull(true)};
 		else{
 			objectPulls = new ObjectPull[numberOfFragmentsOfStorage];
 			for(int i=0; i<objectPulls.length; i++)
 				objectPulls[i] = new ObjectPull(true);
 		}
 		IndividualsManager.setManager(this);
 	}
 
 	@Override
 	public Male getMale(Genotype genotype, int age, Zone zone) {
		int temp = curSubStorage++;
		return objectPulls[(temp>=objectPulls.length)?(curSubStorage=0):temp].getMale(genotype, age, zone);
 	}
 
 	@Override
 	public Female getFemale(Genotype genotype, int age, Zone zone) {
		int temp = curSubStorage++;
		return objectPulls[(temp>=objectPulls.length)?(curSubStorage=0):temp].getFemale(genotype, age, zone);
 	}
 
 	@Override
 	public void killMale(Male male) {
		int temp = curSubStorage++;
		objectPulls[(temp>=objectPulls.length)?(curSubStorage=0):temp].killMale(male);
 	}
 
 	@Override
 	public void killFemale(Female female) {
		int temp = curSubStorage++;
		objectPulls[(temp>=objectPulls.length)?(curSubStorage=0):temp].killFemale(female);
 	}
 }
