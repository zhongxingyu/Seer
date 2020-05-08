 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.util.Log;
 
 
 public class PortalToTown extends UseableObject {
 
 	public PortalToTown(GameCore mCore) throws SlickException {
 		super("./res/items/town-portal.cfg", mCore);
 	}
 
 	@Override
 	public boolean doEffect(Minion mUnit) {
 		for ( Place p: getCore().getPlaces() )
 		{
 			if ( p.getName().compareToIgnoreCase("town") == 0 )
 			{
 				p.addUser(mUnit);
 				return true;
 			}
 		}
 		Log.warn("Minion attempted to go to a town, but there is no town!");
 		return false;
 	}
 
 	@Override
 	public String getTypeName() {
 		return "Portal To Town";
 	}
 
	@Override
	public void upgrade() {
	}

	@Override
	public boolean doEffect(Minion m, String string) {
        return doEffect(m);
	}
 }
