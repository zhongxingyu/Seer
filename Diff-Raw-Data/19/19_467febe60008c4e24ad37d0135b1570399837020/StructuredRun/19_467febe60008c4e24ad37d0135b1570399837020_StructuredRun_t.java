 package pure_thermal;
 
 
 import j.h.entities.CoolingPlan;
 import j.h.entities.Schedule;
 import j.h.entities.Thermal;
 import j.h.entities.ThermalUpdate;
 
 /**
  *
  * @author fsmm2
  */
 public class StructuredRun {
     ThermalUpdate up = new ThermalUpdate();
     DataCenter data=new DataCenter();
     int count=0;
             double[][] list;
             double air;
             Thermal t=new Thermal();
             CoolingPlan c=new CoolingPlan();
             Schedule s = new Schedule();
             int run;
     public StructuredRun()
     {
         //
         //
         //IMPORTANT: SENDING DOUBLES IN CELSIUS, EXPECTING DATA BACK AS KELVIN
         //
         //
 double[][] init=data.getTempArray();
 double init_air=data.getAirTemp();
 t.Submit(init, init_air);
 //pass list to cooling, get cooling plan.
 c.getCoolingPlan();
 data.setCoolingPlan(c.getNum());
     }
     public StructuredRun(DataCenter d, int runs)
     {
         data=d;
         count=runs;
     }
 
     public  void main()
     {
         up.ChangeStatus(true);
         count++;
         //data=DB.getData;
         DataCenter data=new DataCenter();
         //data.setTempArray(t.getTemp);
 DataCenter run1=new DataCenter();
 DataCenter run2=new DataCenter();
 DataCenter run3=new DataCenter();
 DataCenter run4=new DataCenter();
 DataCenter[] runs={run1, run2, run3, run4};
 boolean accepted1=false, accepted2=false, accepted3=false, accepted4=false;
 boolean[] results={accepted1, accepted2, accepted3, accepted4};
 boolean reply=false;
 //DB.getRun();
 if(count==30)
 {
     c.getCoolingPlan();
     //get new cooling plan from cooling.
     data.setCoolingPlan(c.getNum());
     //send air temp to power.
     data.resetAirTemp();
 }
     //1. Pass initial thermal map to optimization
 
     //2. Receive initial cooling plan from optimization
     //These steps not necessary if data center always starts at default state.
 //data.updateCooling(cooling input);
     //3. Receive schedule from scheduling team
 
 
     //4. run thermal simulator. Save results to temporary entry (i.e. [time]run1)
     //power uses this reading.
 int i=0;
 //while(accepted1==false&&accepted2==false&&accepted3==false&&accepted4==false)
 //{
     if(i<4)
     {
runs[i].getAllAttributes(data);
 s.getSchedule();
 double[][] schedulingInput = new double[4][10];
 schedulingInput=s.getPlant();
 runs[i].setIntensityPlan(runs[i].flattenArray(schedulingInput));
 //runs[i].setIntensityPlan(schedulingInput);
 runs[i].runArray(1);
 list=runs[i].getTempArray();
 air=runs[i].getAirTemp();
 //send list to DB.
 t.Submit(list, air);
 //wait for reply.
 //specifically, the trigger from optimization about which run to use.
 //this should change one of the boolean accepted(i) to true, exiting the while loop
 results[i]=reply;
 if(results[i]==true)
    data.getAllAttributes(runs[i]);
     i++;
     }
  else
     {
         //wait for conformation from DB about which run to do.
         if(accepted1==true)
            data.getAllAttributes(run1);
         if(accepted2==true)
            data.getAllAttributes(run2);
         if(accepted3==true)
            data.getAllAttributes(run3);
         if(accepted4==true)
            data.getAllAttributes(run4);
 
  }
         //}
     //5. Wait for further input from optimization.
     //true - write to permanent file
     //false- wait for further input from scheduling team for this epoch.
     //WE SHOULD USE THE EPOCHS AS IDENTIFIERS
 
     //repeat step 4. save temporary to [time]run2.
     //loop
     //if 4 temporary runs saved, wait for input from optimization to say which file best
     //write to permanent file
 
 
     //after every 30 commits, decrease air temp based on cooling. SECONDARY CONCERN
 
     //after 30 commits, check and update cooling plan. Send air temp to power
     //for now, just reset air temp to default
 
 
     //
 up.ChangeStatus(false);
 }
 }

