 package utils.individuals.allocation;
 
 import genotype.Genotype;
 import individual.Female;
 import individual.Male;
 import zone.Zone;
 
 public class SynchronizedObjectPull implements IIndividualsManager {
 
 	private Male[] maleStorage;
 	private Female[] femaleStorage;
 	private int	numberOfMales,
 				numberOfFemales;
 	
 	public SynchronizedObjectPull(){
 		maleStorage = new Male[1];
 		femaleStorage = new Female[1];
 		numberOfMales=0;
 		numberOfFemales=0;
 	}
 	
 	@Override
 	public Male getMale(Genotype genotype, int age, Zone zone){
 		Male male=null;
 		synchronized (maleStorage) {
 			if (numberOfMales==0)
 				return new Male(genotype, age, zone);
 			male = removeMale();
 		}
 		return (Male)male.reset(genotype, age, zone);
 	}
 	
 	@Override
 	public Female getFemale(Genotype genotype, int age, Zone zone){
 		Female female = null;
 		synchronized (femaleStorage) {
 			if (numberOfFemales==0)
 				return new Female(genotype, age, zone);
 			female = removeFemale();
 		}
 		return (Female)female.reset(genotype, age, zone);
 	}
 
 	@Override
 	public void killMale(Male male) {
 		synchronized (maleStorage) {
 			if (maleStorage.length <= numberOfMales)
 				increaseMaleStorage();
 			maleStorage[numberOfMales++] = male;
 		}
 	}
 
 	@Override
 	public void killFemale(Female female) {
 		synchronized (femaleStorage) {
 			if (femaleStorage.length <= numberOfFemales)
 				increaseFemaleStorage();
 			femaleStorage[numberOfFemales++] = female;
 		}
 	}
 	
 	Male removeMale(){
		synchronized (maleStorage) {
			return maleStorage[--numberOfMales];
		}
 	}
 	
 	Female removeFemale(){
		synchronized (femaleStorage) {
			return femaleStorage[--numberOfFemales];
		}
 	}
 	
 	private void increaseMaleStorage(){
		synchronized (maleStorage) {
			Male[] temp = maleStorage;
			maleStorage = new Male[temp.length*2];
			for(int i=0; i<temp.length; i++)
				maleStorage[i] = temp[i];
		}
 	}
 	
 	private void increaseFemaleStorage(){
		synchronized (femaleStorage) {
			Female[] temp = femaleStorage;
			femaleStorage = new Female[temp.length*2];
			for(int i=0; i<temp.length; i++)
				femaleStorage[i] = temp[i];
		}
 	}
 
 	public int getCapacityOfPull() {
 		return femaleStorage.length + maleStorage.length;
 	}
 }
