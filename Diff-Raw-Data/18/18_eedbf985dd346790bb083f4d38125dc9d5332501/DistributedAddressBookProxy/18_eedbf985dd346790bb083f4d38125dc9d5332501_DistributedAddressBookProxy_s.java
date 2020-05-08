 package dacus.core.subscriber;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
import dacus.model.contactprofile.tech.party.Party;
 import dacus.model.sub.DistributedAddressBook;
 
 // FIXME: implement proxy as gateway to DAB
 public class DistributedAddressBookProxy implements DistributedAddressBook {
 
 	private DistributedAddressBook distributedAddressBook;
 	
 	public DistributedAddressBookProxy(DistributedAddressBook distributedAddressBook) {
 		this.distributedAddressBook = distributedAddressBook;
 	}
 	
 	@Override
 	public boolean add(Party e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public void add(int index, Party element) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public boolean addAll(Collection<? extends Party> c) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public boolean addAll(int index, Collection<? extends Party> c) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public void clear() {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public boolean contains(Object o) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public boolean containsAll(Collection<?> c) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public Party get(int index) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public int indexOf(Object o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 	@Override
 	public boolean isEmpty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public Iterator<Party> iterator() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public int lastIndexOf(Object o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 	@Override
 	public ListIterator<Party> listIterator() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public ListIterator<Party> listIterator(int index) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public boolean remove(Object o) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public Party remove(int index) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public boolean removeAll(Collection<?> c) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public boolean retainAll(Collection<?> c) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public Party set(int index, Party element) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public int size() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 	@Override
 	public List<Party> subList(int fromIndex, int toIndex) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public Object[] toArray() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public <T> T[] toArray(T[] a) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
