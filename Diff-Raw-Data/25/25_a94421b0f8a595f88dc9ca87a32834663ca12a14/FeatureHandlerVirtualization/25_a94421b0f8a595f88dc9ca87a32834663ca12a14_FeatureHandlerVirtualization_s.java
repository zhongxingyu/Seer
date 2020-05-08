 package ca.uwaterloo.lkc;
 
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.gnome.gtk.Button;
 import org.gnome.gtk.CheckButton;
 import org.gnome.gtk.Stock;
 
 import ca.uwaterloo.lkc.FeatureScreenHandler.Features;
 
 public class FeatureHandlerVirtualization extends FeatureHandler {
 
     public static final Map<FeatureScreenHandler.Features, CheckButton> buttonMap = new TreeMap<FeatureScreenHandler.Features, CheckButton>() {{ 
         put(Features.KVM, new CheckButton("KVM"));
         put(Features.XEN, new CheckButton("XEN"));
     }};
     
     final static String description = "Support hosting fully virtualized guest machines using hardware " +
     		"virtualization extensions. You will need a fairly recent " +
     		"processor equipped with virtualization extensions. You will also " +
     		"need to select one or more of the processor modules below.\n\n" +
     		"This module provides access to the hardware capabilities through " +
     		"a character device node named /dev/kvm.\n\n";
     
     final static String descriptionXEN = "This is the Linux Xen port. Enabling this will allow the " +
     		"kernel to boot in a paravirtualized environment under the " +
     		"Xen hypervisor.";
      
     FeatureHandlerVirtualization(final FeatureScreenHandler fsh)
     {
         this.fsh = fsh;
         
         for (int i = 0; i < buttonMap.size(); ++i)
         {
             fsh.layOption.put((CheckButton) buttonMap.values().toArray()[i], 0, i * 23);
         }
         
         selectedOptions.add(Features.None);
         selectedOptions.add(Features.None);
         
         featureMap.put(Features.KVM, new Feature(fsh, description, 0, Stability.Stable));
         featureMap.put(Features.XEN, new Feature(fsh, descriptionXEN, 300000, Stability.Stable));
 
         buttonMap.get(Features.KVM).connect(new Button.Clicked() {
             
             @Override
             public void onClicked(Button arg0) {
                 // TODO Auto-generated method stub
                 featureMap.get(Features.KVM).updateUI();
                 selectedOptions.set(0, buttonMap.get(Features.KVM).getActive() ? Features.KVM : Features.None);
             }
         });
         
         buttonMap.get(Features.XEN).connect(new Button.Clicked() {
             
             @Override
             public void onClicked(Button arg0) {
                 // TODO Auto-generated method stub
                 featureMap.get(Features.XEN).updateUI();
                 selectedOptions.set(1, buttonMap.get(Features.XEN).getActive() ? Features.XEN : Features.None);
             }
         });
 
 
     }
     
     @Override
     public String getQuestion() {
         return "Do you want to support virtualization?";
     }
     public void updateUI()
     {
         buttonMap.get(Features.KVM).grabFocus();
         for (Features f : selectedOptions)
         {
             if (Features.None != f)
             {
                 featureMap.get(f).updateUI();
                 buttonMap.get(f).setActive(true);
             }
         }
         fsh.updateFeatureDescription(description);
     }
     
     @Override
     public void show() {
         updateUI();
         for (CheckButton rb : buttonMap.values())
         {
             rb.show();
         }
     }
 
     @Override
     public boolean isRelevant(Vector<Features> v) {
         return !v.contains(Features.Minimum);
     }
 
     @Override
     public String getInstruction() {
         return "Select options and click Next";
     }
 
     @Override
     public int getSize() {
         int size = 0;
         
         for (Features f : selectedOptions)
         {
             if (Features.None != f)
             {
                 size += featureMap.get(f).size;
             }
         }
         return size;
     }
 
     @Override
     public Stability getStability() {
         Stability s = Stability.Stable;
         
         for (Features f : selectedOptions)
         {
             if (Features.None != f)
             {
                 s = FeatureHandler.minStability(s, featureMap.get(f).stability);
             }
         }
         return s;
     }
 
     @Override
     public int getNum() {
         int size = 0;
         
         for (Features f : selectedOptions)
         {
             if (Features.None != f)
             {
                 ++size;
             }
         }
         return size;
     }
 
     @Override
     public Stock getImage() {
         return Stock.EXECUTE;
     }
 
     @Override
     public String getName() {
         return "Virtualization";
     }
 
     @Override
     public void setDefault() {
         Vector<Features> v = new Vector<Features>();
         v.add(Features.None);
         v.add(Features.None);
         load(v);
     }
 }
