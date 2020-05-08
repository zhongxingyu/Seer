 import java.util.Comparator;
 
 public class FormeComparatorFactory {
 
 	public Comparator<Forme> getComparator(FormeComparatorType type) {
 		Comparator<Forme> comparator = null;
 
 		switch (type) {
 		case AIRE_ASC:
 			comparator = new AireComparator();
 			break;
 		case AIRE_DESC:
 			comparator = Ensembles.reverseOrder(new AireComparator());
 			break;
 		case DISTANCE_MAXIMALE_ASC:
 			comparator = new DistanceMaxComparator();
 			break;
 		case DISTANCE_MAXIMALE_DESC:
 			comparator = Ensembles.reverseOrder(new DistanceMaxComparator());
 			break;
 		case NO_SEQUENTIEL_ASC:
 			comparator = new NoSequentielComparator();
 			break;
 		case NO_SEQUENTIEL_DESC:
 			comparator = Ensembles.reverseOrder(new NoSequentielComparator());
 			break;
 		case TYPE_FORME_ASC:
 			comparator = new TypeComparator();
 			break;
 		case TYPE_FORME_DESC:
 			comparator = Ensembles.reverseOrder(new TypeComparator());
 			break;
 		case HAUTEUR_ASC:
 			comparator = new HauteurComparator();
 			break;
 		case HAUTEUR_DESC:
 			comparator = Ensembles.reverseOrder(new HauteurComparator());
 			break;
 		case LARGEUR_ASC:
 			comparator = new LargeurComparator();
 			break;
 		case LARGEUR_DESC:
 			comparator = Ensembles.reverseOrder(new LargeurComparator());
 			break;
 		case AUCUN:
 			comparator = null;
 			break;
 		default:
 			break;
 		}
 
 		return comparator;
 	}
 
 	private class NoSequentielComparator implements Comparator<Forme> {
 		@Override
 		public int compare(Forme lhs, Forme rhs) {
 			return Integer.compare(lhs.getNoSeq(), rhs.getNoSeq());
 		}
 	}
 
 	private class AireComparator implements Comparator<Forme> {
 		@Override
 		public int compare(Forme lhs, Forme rhs) {
 			return Double.compare(lhs.getAire(), rhs.getAire());
 		}
 
 	}
 
 	private class TypeComparator implements Comparator<Forme> {
 		@Override
 		public int compare(Forme lhs, Forme rhs) {
 			return Integer.compare(lhs.getType().ordinal(), rhs.getType()
 					.ordinal());
 		}
 	}
 
 	private class DistanceMaxComparator implements Comparator<Forme> {
 		@Override
 		public int compare(Forme lhs, Forme rhs) {
 			return Double.compare(lhs.getDistanceMax(), rhs.getDistanceMax());
 		}
 	}
 
 	private class LargeurComparator implements Comparator<Forme> {
 
 		@Override
 		public int compare(Forme o1, Forme o2) {
 			return Integer.compare(o1.getWidth(), o2.getWidth());
 		}
 	}
 
 	private class HauteurComparator implements Comparator<Forme> {
 		@Override
 		public int compare(Forme o1, Forme o2) {
			return Integer.compare(o1.getHeight(), o2.getHeight());
 		}
 	}
 
 }
