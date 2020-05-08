 package out;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 
 import mutate.mutList;
 import haplos.hap;
 
 public class out {
 
 	boolean infSites = false;
 	double[] sortVals;
 	public out(){
 		
 	}
 	
 	public void setInfiniteSites(boolean aBol){
 		infSites = aBol;
 	}
 	public void printHaps(String fileBase,int length,mutList aMutList, hap haplos) throws IOException{
 		int ipop,ichr,im,nchr,i,jm;
 		int[] chrCount,mutSort,mutCount = null,useMut = null;
 		String fileName , idstr;
 		int[] aHap;
 		double freq;
 		
 		chrCount = new int[haplos.getnPop()];
 		mutSort = new int[aMutList.getNMut()];
 		useMut = new int[aMutList.getNMut()];
 		aHap = new int[aMutList.getNMut()];
 		
 		for(ichr = 0;ichr<haplos.getNChrom();ichr++){
 			chrCount[haplos.getWhichPopInd()[ichr]]++;
 		}
 		
 		for(im=0;im<aMutList.getNMut();im++){
 			//mutSort[im]= im;
 			useMut[im] = 1;
 			
 		}
 		this.setSortVals(aMutList.getPos());
 		//sort mutSort according to sortVals...
		double[] tempVal = deepCopy(sortVals,aMutList.getNMut());
 		Arrays.sort(tempVal);
 		for(i=0;i<tempVal.length;i++){//
 			double temp = sortVals[i];
 			mutSort[Arrays.binarySearch(tempVal, temp)] = i;
 		}
 		
 		if(!infSites){
 			for(im =0;im<aMutList.getNMut();im++){
 				jm = im +1;
 				while(jm<aMutList.getNMut() && (int) (length* aMutList.getPos()[mutSort[im]])== (int) (length*aMutList.getPos()[mutSort[jm]])){
 					useMut[mutSort[jm]] = 0;
 					jm++;
 				}
 			}
 		}
 		for(ipop = 0;ipop<haplos.getnPop();ipop++){
 			if(chrCount[ipop]>0){
 				fileName = fileBase + ".hap-" + haplos.getPopId()[ipop];
 				File outFile = new File(fileName);
 				outFile.createNewFile();
 				FileWriter outWriter = new FileWriter(outFile,true);
 				mutCount = new int[aMutList.getNMut()];
 				
 				
 				nchr = 0;
 				for(ichr =0;ichr<haplos.getNChrom();ichr++){
 					if(haplos.getWhichPopInd()[ichr]!=ipop){continue;}
 					outWriter.write(String.format("%d\t%d\t",ichr,haplos.getPopId()[ipop]));
 					for(im=0;im<aMutList.getNMut();im++){aHap[im] = 2;}
 					for(i=0;i<haplos.getnMut()[ichr];i++){
						aHap[haplos.getMutIndex()[ichr][i]] = 1;
						mutCount[haplos.getMutIndex()[ichr][i]]++;
 					}
 					for(im=0;im<aMutList.getNMut();im++){
 						if(useMut[mutSort[im]]==1){
 							outWriter.write(String.format("%d",aHap[mutSort[im]]));
 						}
 					}
 					outWriter.write("\n");
 					nchr++;
 					
 				}
 				outWriter.close();
 				fileName = fileBase + ".pos-" + haplos.getPopId()[ipop];
 				outFile = new File(fileName);
 				outFile.createNewFile();
 				outWriter = new FileWriter(outFile,true);
 				outWriter.write("SNP\tCHROM\tCHROM_POS\tALLELE1\tFREQ1\tALLELE2\tFREQ2\n");
 				for(im=0;im<aMutList.getNMut();im++){
 					freq = (double) mutCount[mutSort[im]]/chrCount[ipop];
 					if(useMut[mutSort[im]]==1){
 						if(infSites){
 							outWriter.write(String.format("%d\t1\t%.4f\t1\t%.4f\t2\t%.4f\n",im+1,length*aMutList.getPos()[mutSort[im]],freq,1-freq));
 							
 						}
 						else{
 							if(im>0 && (int)(length*aMutList.getPos()[mutSort[im]])== (int)(length*aMutList.getPos()[mutSort[im-1]])){
 								System.out.println(String.format("double hit: %f & %f",length*aMutList.getPos()[mutSort[im]],length*aMutList.getPos()[mutSort[im-1]]));
 							}
 							outWriter.write(String.format("%d\t1\t%d\t1\t%.4f\t2\t%.4f\n",im+1,(int)(length*aMutList.getPos()[mutSort[im]]),freq,1-freq));
 						}
 					}
 				}
 				outWriter.close();
 			}
 		}
 		
 		
 		
 		
 		
 		
 		
 		
 		
 	}
 	public void setSortVals(double[] aSort){
 		sortVals = aSort;
 	}
	private double[] deepCopy(double[] anArray,int size){
		double[] temp = new double[size];
		for(int i=0;i<size;i++){
 			temp[i] = anArray[i];
 		}
 		return temp;
 	}
 	
 }
