 package com.rubensworks.minerva.sdk.fetch;
 
 import com.rubensworks.minerva.sdk.DataHolder;
 import com.rubensworks.minerva.sdk.Minerva;
 
 public abstract class Fetch {
 	protected volatile DataHolder data;
 	protected ExecutionDataHolder defaultExecutionDataHolder=new ExecutionDataHolder() {
 
 		@Override
 		public void onError(Exception e) {
			System.out.println(e.getMessage());
 			System.out.println("error");
 		}
 
 		@Override
 		public void onComplete(DataHolder data) {
 			Fetch.this.data=data;
 		}
 		
 	};
 	
 	/**
 	 * Will sleep untill the data has been fetched
 	 * @param minerva
 	 * @return
 	 */
 	protected boolean dataHolderSleeper(Minerva minerva) {
 		while(data==null) {
 			if(minerva.isError())
 				return false;
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 				return false;
 			}
 		}
 		
 		if(data.getData()==null 
 				|| data.getData().length==0 
				//|| data.getData()[0].getData()==null 
				//|| data.getData()[0].getData().length==0
 				)
 			return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Will wait to return untill the data has been fetched
 	 * @param minerva
 	 * @return
 	 */
 	public DataHolder fetch(Minerva minerva) {
 		if(!dataHolderSleeper(minerva)) return null;
 		return data;
 	}
 }
