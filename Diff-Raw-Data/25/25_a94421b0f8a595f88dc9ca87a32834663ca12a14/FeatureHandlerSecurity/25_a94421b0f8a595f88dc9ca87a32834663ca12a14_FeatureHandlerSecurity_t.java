 package ca.uwaterloo.lkc;
 
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.gnome.gtk.Button;
 import org.gnome.gtk.CheckButton;
 import org.gnome.gtk.Stock;
 
 import ca.uwaterloo.lkc.FeatureScreenHandler.Features;
 
 public class FeatureHandlerSecurity extends FeatureHandler {
 
     public static final Map<FeatureScreenHandler.Features, CheckButton> buttonMap = new TreeMap<FeatureScreenHandler.Features, CheckButton>() {{ 
         put(Features.SELinux, new CheckButton("SELinux"));
         put(Features.CryptoAPI, new CheckButton("CryptoApi"));
     }};
     
     final static String description = "This selects NSA Security-Enhanced Linux (SELinux). " +
     		"You will also need a policy configuration and a labeled filesystem. " +
     		"If you are unsure how to answer this question, answer \"No\".";
     
     final static String descriptionCrypto = "This adds cryptographic functions to the kernel. " +
     		"Select this option if you plan to frequently use cryptography, as it may speed-up computations.";
     
     FeatureHandlerSecurity(final FeatureScreenHandler fsh)
     {
         this.fsh = fsh;
         for (int i = 0; i < buttonMap.size(); ++i)
         {
             fsh.layOption.put((CheckButton) buttonMap.values().toArray()[i], 0, i * 23);
         }
         
         selectedOptions.add(Features.None);
         selectedOptions.add(Features.None);
 
         featureMap.put(Features.SELinux, new Feature(fsh, description, 200000, Stability.Stable));
         featureMap.put(Features.CryptoAPI, new Feature(fsh, descriptionCrypto, 300000, Stability.Stable));
         
         buttonMap.get(Features.SELinux).connect(new Button.Clicked() {
             
             @Override
             public void onClicked(Button arg0) {
                 // TODO Auto-generated method stub
                 featureMap.get(Features.SELinux).updateUI();
                 selectedOptions.set(0, buttonMap.get(Features.SELinux).getActive() ?  Features.SELinux : Features.None);
             }
         });
         
         buttonMap.get(Features.CryptoAPI).connect(new Button.Clicked() {
             
             @Override
             public void onClicked(Button arg0) {
                 // TODO Auto-generated method stub
                 featureMap.get(Features.CryptoAPI).updateUI();
                 selectedOptions.set(1, buttonMap.get(Features.CryptoAPI).getActive() ? Features.CryptoAPI : Features.None);
             }
         });
 
     }
     
     @Override
     public String getQuestion() {
         return "Select security features";
     }
 
 
     public void updateUI()
     {
         buttonMap.get(Features.SELinux).grabFocus();
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
         // TODO Auto-generated method stub
         return Stock.DIALOG_AUTHENTICATION;
     }
 
     @Override
     public String getName() {
         return "Security";
     }
 
     @Override
     public void setDefault() {
        for (CheckButton c : buttonMap.values())
        {
            c.setActive(false);
        }
         Vector<Features> v = new Vector<Features>();
         v.add(Features.None);
         v.add(Features.None);
         load(v);
     }
 }
