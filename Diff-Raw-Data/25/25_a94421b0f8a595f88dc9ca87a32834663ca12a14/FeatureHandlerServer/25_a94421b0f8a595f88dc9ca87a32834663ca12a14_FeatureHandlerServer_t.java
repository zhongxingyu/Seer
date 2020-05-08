 package ca.uwaterloo.lkc;
 
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.gnome.gtk.Button;
 import org.gnome.gtk.CheckButton;
 import org.gnome.gtk.Stock;
 
 import ca.uwaterloo.lkc.FeatureScreenHandler.Features;
 
 public class FeatureHandlerServer extends FeatureHandler {
 
     public static final Map<FeatureScreenHandler.Features, CheckButton> buttonMap = new TreeMap<FeatureScreenHandler.Features, CheckButton>() {{ 
         put(Features.Netfilter, new CheckButton("Netfilter"));
         put(Features.Qos, new CheckButton("Qos"));
     }};
         
     final static String description = "Netfilter is a framework for filtering and mangling network packets " +
     		"that pass through your Linux box. " +
     		"The most common use of packet filtering is to run your Linux box as " +
     		"a firewall protecting a local network from the Internet. The type of " +
     		"firewall provided by this kernel support is called a \"packet " +
     		"filter\", which means that it can reject individual network packets " +
     		"based on type, source, destination etc. The other kind of firewall, " +
     		"a \"proxy-based\" one, is more secure but more intrusive and more " +
     		"bothersome to set up; it inspects the network traffic much more " +
     		"closely, modifies it and has knowledge about the higher level " +
     		"protocols, which a packet filter lacks. Moreover, proxy-based " +
     		"firewalls often require changes to the programs running on the local " +
     		"clients. Proxy-based firewalls don't need support by the kernel, but " +
     		"they are often combined with a packet filter, which only works if " +
     		"you say \"Yes\" here.\n\n" +
     		"You should also say \"Yes\" here if you intend to use your Linux box as " +
     		"the gateway to the Internet for a local network of machines without " +
     		"globally valid IP addresses. This is called \"masquerading\": if one " +
     		"of the computers on your local network wants to send something to " +
     		"the outside, your box can \"masquerade\" as that computer, i.e. it " +
     		"forwards the traffic to the intended outside destination, but " +
     		"modifies the packets to make it look like they came from the " +
     		"firewall box itself. It works both ways: if the outside host " +
     		"replies, the Linux box will silently forward the traffic to the " +
     		"correct local computer. This way, the computers on your local net " +
     		"are completely invisible to the outside world, even though they can " +
     		"reach the outside and can receive replies. It is even possible to " +
     		"run globally visible servers from within a masqueraded local network " +
     		"using a mechanism called portforwarding. Masquerading is also often " +
     		"called NAT (Network Address Translation).\n\n" +
     		"Another use of Netfilter is in transparent proxying: if a machine on " +
     		"the local network tries to connect to an outside host, your Linux " +
     		"box can transparently forward the traffic to a local server, " +
     		"typically a caching proxy server.\n\n" +
     		"Yet another use of Netfilter is building a bridging firewall. Using " +
     		"a bridge with Network packet filtering enabled makes iptables \"see\" " +
     		"the bridged traffic. For filtering on the lower network and Ethernet " +
     		"protocols over the bridge, use ebtables (under bridge netfilter " +
     		"configuration).\n\n" +
     		"Various modules exist for netfilter which replace the previous " +
     		"masquerading (ipmasqadm), packet filtering (ipchains), transparent " +
     		"proxying, and portforwarding mechanisms. Please see " +
     		"<file:Documentation/Changes> under \"iptables\" for the location of " +
     		"these packages.";
     
     final static String descriptionQos = "When the kernel has several packets to send out over a network " +
     		"device, it has to decide which ones to send first, which ones to " +
     		"delay, and which ones to drop. This is the job of the queueing " +
     		"disciplines, several different algorithms for how to do this " +
     		"\"fairly\" have been proposed.\n\n" +
     		"If you say \"No\" here, you will get the standard packet scheduler, which " +
     		"is a FIFO (first come, first served). If you say \"Yes\" here, you will be " +
     		"able to choose from among several alternative algorithms which can " +
     		"then be attached to different network devices. This is useful for " +
     		"example if some of your network devices are real time devices that " +
     		"need a certain minimum data flow rate, or if you need to limit the " +
     		"maximum data flow rate for traffic which matches specified criteria. " +
     		"This code is considered to be experimental.\n\n" +
     		"To administer these schedulers, you'll need the user-level utilities " +
     		"from the package iproute2+tc at <ftp://ftp.tux.org/pub/net/ip-routing/>. " +
     		"That package also contains some documentation; for more, check out " +
     		"<http://linux-net.osdl.org/index.php/Iproute2>.\n\n" +
     		"This Quality of Service (QoS) support will enable you to use " +
     		"Differentiated Services (diffserv) and Resource Reservation Protocol " +
     		"(RSVP) on your Linux router if you also say Y to the corresponding " +
     		"classifiers below. Documentation and software is at " +
     		"<http://diffserv.sourceforge.net/>.\n\n" +
     		"If unsure, say \"No\" now.";
     
     FeatureHandlerServer(final FeatureScreenHandler fsh)
     {
         this.fsh = fsh;
         
         for (int i = 0; i < buttonMap.size(); ++i)
         {
             fsh.layOption.put((CheckButton) buttonMap.values().toArray()[i], 0, i * 23);
         }
         
         selectedOptions.add(Features.None);
         selectedOptions.add(Features.None);
         
         featureMap.put(Features.Netfilter, new Feature(fsh, description, 300000, Stability.Stable));
         featureMap.put(Features.Qos, new Feature(fsh, descriptionQos, 600000, Stability.Warning));
         
         buttonMap.get(Features.Netfilter).connect(new Button.Clicked() {
             
             @Override
             public void onClicked(Button arg0) {
                 // TODO Auto-generated method stub
                 featureMap.get(Features.Netfilter).updateUI();
                 selectedOptions.set(1, buttonMap.get(Features.Netfilter).getActive() ? Features.Netfilter : Features.None);
             }
         });
         
         buttonMap.get(Features.Qos).connect(new Button.Clicked() {
             
             @Override
             public void onClicked(Button arg0) {
                 // TODO Auto-generated method stub
                 featureMap.get(Features.Qos).updateUI();
                 selectedOptions.set(1, buttonMap.get(Features.Qos).getActive() ? Features.Qos : Features.None);
             }
         });
 
     }
     
     @Override
     public String getQuestion() {
         return "Select server features";
     }
 
     public void updateUI()
     {        
         buttonMap.get(Features.Netfilter).grabFocus();
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
         return v.contains(Features.Server);
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
         return Stock.NETWORK;
     }
 
     @Override
     public String getName() {
         return "Server";
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
