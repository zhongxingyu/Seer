 package bee.creative.util;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Diese abstrakte Klasse implementiert die Basis einer {@link Object#hashCode() Streuwert}-basierten Abbildung von
  * Schlüsseln auf Werte. Die Einträge der Abbildung besitzen einen nächsten Eintrag, sodass einfach verkettete Listen
  * von Einträgen erzeugt werden können. Der nächste Eintrag eines Eintrags muss dazü mit
  * {@link Hash#getEntryNext(Object)} gelesen und mit {@link Hash#setEntryNext(Object, Object)} geschrieben werden
  * können. Als Schlüssel und Werte sind beliebige Objekte zulässig. Insbesondere ist es möglich, die Werte der Abbildung
  * als Einträge zu verwenden, sofern diese über einen Schlüssel und ein nächsten Element verfügen. Es ist auch möglich
  * für Schlüssel und Wert eines Eintrags das gleiche Objekt zu nutzen.
  * <p>
  * Die Einträge werden in einfach verketteten Listen verwaltet, deren Kopfelemente bzw. Einträge in einer Tabelle
  * hinterlegt werden. Die Methoden {@link Hash#getKeyHash(Object)} muss zu einem gegebenen Schlüssel den
  * {@link Object#hashCode() Streuwert} berechnen, und die Methode {@link Hash#getIndex(int, int)} muss zu einem
  * gegebenen {@link Object#hashCode() Streuwert} den Index des Eintrags in der Tabelle berechnen, in dessen einfach
  * verketteter Liste sich der Eintrag mit dem gegebenen Schlüssen bzw. {@link Object#hashCode() Streuwert} befindet.
  * 
  * @author [cc-by] 2011 Sebastian Rostock [http://creativecommons.org/licenses/by/3.0/de/]
  * @param <GKey> Typ der Schlüssel.
  * @param <GValue> Typ der Werte.
  * @param <GEntry> Typ der Einträge.
  */
 public abstract class Hash<GKey, GValue, GEntry> {
 
 	/**
 	 * Diese Klasse implementiert den {@link Iterator} über die Einträge der Abbildung.
 	 * 
 	 * @author [cc-by] 2011 Sebastian Rostock [http://creativecommons.org/licenses/by/3.0/de/]
 	 * @param <GKey> Typ der Schlüssel.
 	 * @param <GEntry> Typ der Einträge.
 	 */
 	protected static final class HashIterator<GKey, GEntry> implements Iterator<GEntry> {
 
 		/**
 		 * Dieses Feld speichert die Abbildung.
 		 */
 		final Hash<GKey, ?, GEntry> hash;
 
 		/**
 		 * Dieses Feld speichert den nächsten Eintrag, der von {@link Hash.HashIterator#next()} zurück gegeben wird.
 		 */
 		GEntry next;
 
 		/**
 		 * Dieses Feld speichert den letzten Eintrag, der von {@link Hash.HashIterator#next()} zurück gegeben wurde.
 		 */
 		GEntry last;
 
 		/**
 		 * Dieses Feld speichert die Position der aktuellen einfach verketteten Liste in der Tabelle.
 		 */
 		int index;
 
 		/**
 		 * Dieser Konstrukteur initialisiert die Abbildung und sucht den ersten Eintrag.
 		 * 
 		 * @param hash Abbildung.
 		 */
 		public HashIterator(final Hash<GKey, ?, GEntry> hash) {
 			this.hash = hash;
 			this.seek();
 		}
 
 		/**
 		 * Diese Methode sucht den nächsten Eintrag.
 		 */
 		@SuppressWarnings ({"unchecked"})
 		final void seek() {
 			final GEntry[] table = (GEntry[])this.hash.table;
 			for(int index = this.index, length = table.length; index < length; index++){
 				final GEntry next = table[index];
 				if(next != null){
 					this.next = next;
 					this.index = index + 1;
 					return;
 				}
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public final boolean hasNext() {
 			return this.next != null;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public GEntry next() {
 			final GEntry item = this.next;
 			if(item == null) throw new NoSuchElementException();
 			final GEntry next = this.hash.getEntryNext(item);
 			if(next == null){
 				this.seek();
 			}else{
 				this.next = next;
 			}
 			return this.last = item;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void remove() {
 			final GEntry last = this.last;
 			if(last == null) throw new IllegalStateException();
 			this.last = null;
 			final Hash<GKey, ?, GEntry> hash = this.hash;
 			hash.removeEntry(hash.getEntryKey(last), false);
 		}
 
 	}
 
 	/**
 	 * Dieses Feld speichert die Anzahl der Einträge.
 	 */
 	int size = 0;
 
 	/**
 	 * Dieses Feld speichert die Tabelle, in der die einfach verketteter Listen der Einträge einsortiert werden.
 	 */
 	Object[] table = new Object[0];
 
 	/**
 	 * Diese Methode soll den {@link Object#hashCode() Streuwert} des gegebenen Schlüssels zurück geben.
 	 * 
 	 * @param key Schlüssel.
 	 * @return {@link Object#hashCode() Streuwert} des Schlüssels.
 	 */
 	protected int getKeyHash(final GKey key) {
 		int hash = Objects.hash(key);
 		hash ^= (hash >>> 20) ^ (hash >>> 12);
 		return hash ^ (hash >>> 7) ^ (hash >>> 4);
 	}
 
 	/**
 	 * Diese Methode soll den Schlüssel des gegebenen Eintrags zurück geben.
 	 * 
 	 * @param entry Eintrag.
 	 * @return Schlüssel des Eintrags.
 	 */
 	protected abstract GKey getEntryKey(GEntry entry);
 
 	/**
 	 * Diese Methode soll den nächsten Eintrag des gegebenen Eintrags zurück geben.
 	 * 
 	 * @param entry Eintrag.
 	 * @return nächster Eintrag des Eintrags oder {@code null}.
 	 */
 	protected abstract GEntry getEntryNext(GEntry entry);
 
 	/**
 	 * Diese Methode soll den nächsten Eintrag des gegebenen Eintrags setzen bzw. aktualisieren.
 	 * 
 	 * @param entry Eintrag.
 	 * @param next nächster Eintrag.
 	 */
 	protected abstract void setEntryNext(GEntry entry, GEntry next);
 
 	/**
 	 * Diese Methode soll den Wert des gegebenen Eintrags zurück geben.
 	 * 
 	 * @param entry Eintrag.
 	 * @return Wert des Eintrags.
 	 */
 	protected abstract GValue getEntryValue(GEntry entry);
 
 	/**
 	 * Diese Methode soll nur dann {@code true} zurück geben, wenn der Schlüssel des gegebenen Eintrags gleich dem
 	 * gegebenen Schlüssel ist.
 	 * 
 	 * @param entry Eintrag.
 	 * @param key Schlüssel.
 	 * @param hash {@link Object#hashCode() Streuwert} des Schlüssels.
 	 * @return {@code true}, wenn der Schlüssel des gegebenen Eintrags gleich dem gegebenen Schlüssel ist.
 	 */
 	protected boolean getEntryEquals(final GEntry entry, final GKey key, final int hash) {
 		return Objects.equals(key, this.getEntryKey(entry));
 	}
 
 	/**
 	 * Diese Methode gibt die Anzahl der Einträge zurück.
 	 * 
 	 * @return Anzahl der Einträge.
 	 */
 	protected final int getSize() {
 		return this.size;
 	}
 
 	/**
 	 * Diese Methode soll den Index des gegebenen {@link Object#hashCode() Streuwert} eines Schlüssels zurück geben. Der
 	 * Index gibt die Position in der Tabelle an, unter der eine einfach verkettete Liste verwaltet wird, deren Einträge
 	 * einen Schlüssel mit diesem {@link Object#hashCode() Streuwert} besitzen können.
 	 * 
 	 * @see Hash#getLength(int, int)
 	 * @param hash {@link Object#hashCode() Streuwert} eines Schlüssels.
 	 * @param length Größe der Tabelle.
 	 * @return Index.
 	 */
 	protected int getIndex(final int hash, final int length) {
 		return hash & (length - 1);
 	}
 
 	/**
 	 * Diese Methode gibt die Größe der Tabelle zurück, in der die Kopfelemente der einfach verketteten Listen der
 	 * Einträge hinterlegt werden.
 	 * 
 	 * @return Größe der Tabelle.
 	 */
 	protected final int getLength() {
 		return this.table.length;
 	}
 
 	/**
 	 * Diese Methode soll die neue Größe der Tabelle zurück geben, in der die Kopfelemente der einfach verketteten Listen
 	 * der Einträge hinterlegt werden.
 	 * 
 	 * @see Hash#getIndex(int, int)
 	 * @param size Anzahl der Einträge
 	 * @param length aktuelle Größe der Tabelle.
 	 * @return neue Größe der Tabelle.
 	 */
 	protected int getLength(int size, int length) {
 		if(size == 0) return 0;
 		if(length == 0){
 			length = 1;
 		}
 		while(length < size){
 			length *= 2;
 		}
 		size *= 2;
 		while(length > size){
 			length /= 2;
 		}
 		return length;
 	}
 
 	/**
 	 * Diese Methode gibt den Eintrag mit dem gegebenen Schlüssel oder {@code null} zurück.
 	 * 
 	 * @param key Schlüssel.
 	 * @return Eintrag mit dem gegebenen Schlüssel oder {@code null}.
 	 */
 	@SuppressWarnings ("unchecked")
 	protected final GEntry findEntry(final GKey key) {
 		if(this.size == 0) return null;
 		final GEntry[] table = (GEntry[])this.table;
 		final int hash = this.getKeyHash(key);
 		final int index = this.getIndex(hash, table.length);
 		for(GEntry entry = table[index]; entry != null; entry = this.getEntryNext(entry)){
 			if(this.getEntryEquals(entry, key, index)) return entry;
 		}
 		return null;
 	}
 
 	/**
 	 * Diese Methode soll einen neuen Eintrag mit dem gegebenen Schlüssel, Wert, nächstem Eintrag sowie
 	 * {@link Object#hashCode() Streuwert} des Schlüssels erzeugen und zurück geben.
 	 * 
 	 * @param key Schlüssel.
 	 * @param value Wert.
 	 * @param next nächster Eintrag.
 	 * @param hash {@link Object#hashCode() Streuwert} des Schlüssels.
 	 * @return neuer Eintrag.
 	 */
 	protected abstract GEntry createEntry(GKey key, GValue value, GEntry next, final int hash);
 
 	/**
 	 * Diese Methode fügt einen neuen Eintrag mit den gegebenen Wert unter dem gegebenen Schlüssel in die Abbildung ein
 	 * und gibt den zuvor unter dem Schlüssel hinterlegten Eintrag oder {@code null} zurück. Wenn die Größe der Tabelle
 	 * {@code 0} ist, wird die Methode {@link Hash#verifyLength()} vor dem Einfügen aufgerufen. Wenn die
 	 * Tabellgrößenenprüfung {@code true} und unter dem gegebenen Schlüssel kein Eintrag registriert sind, wird die
 	 * Methode {@link Hash#verifyLength()} nach dem einfügen des neuen Eintrag aufgerufen.
 	 * 
 	 * @see Hash#verifyLength()
 	 * @param key Schlüssel.
 	 * @param value Wert.
 	 * @param verifyLength Tabellgrößenenprüfung.
 	 * @return alter Eintrag oder {@code null}.
 	 */
 	@SuppressWarnings ("unchecked")
 	protected final GEntry appendEntry(final GKey key, final GValue value, final boolean verifyLength) {
 		final int hash = this.getKeyHash(key);
 		final GEntry[] table = (GEntry[])this.table;
 		if(table.length == 0){
 			this.verifyLength(1);
 			this.size++;
 			this.table[0] = this.createEntry(key, value, null, hash);
 			return null;
 		}
 		final int index = this.getIndex(hash, table.length);
 		for(GEntry last = null, item = table[index], next; item != null; last = item, item = next){
 			next = this.getEntryNext(item);
 			if(this.getEntryEquals(item, key, hash)){
 				if(last == null){
 					table[index] = this.createEntry(key, value, next, hash);
 				}else{
 					this.setEntryNext(last, this.createEntry(key, value, next, hash));
 				}
 				return item;
 			}
 		}
 		this.size++;
 		table[index] = this.createEntry(key, value, table[index], hash);
 		if(!verifyLength) return null;
 		this.verifyLength();
 		return null;
 	}
 
 	/**
 	 * Diese Methode entfernt den Eintrag mit dem gegebenen Schlüssel aus der Abbildung und gibt ihn zurück. Wenn die
 	 * Tabellgrößenenprüfung {@code true} und unter dem gegebenen Schlüssel ein Eintrag registriert sind, wird die Methode
 	 * {@link Hash#verifyLength()} nach dem Entfernen des Eintrags aufgerufen.
 	 * 
 	 * @see Hash#verifyLength()
 	 * @param key Schlüssel.
 	 * @param verifyLength Tabellgrößenenprüfung.
 	 * @return Eintrag oder {@code null}.
 	 */
 	@SuppressWarnings ("unchecked")
 	protected final GEntry removeEntry(final GKey key, final boolean verifyLength) {
 		if(this.size == 0) return null;
 		final GEntry[] table = (GEntry[])this.table;
 		final int hash = this.getKeyHash(key);
 		final int index = this.getIndex(hash, table.length);
 		for(GEntry last = null, item = table[index], next; item != null; last = item, item = next){
 			next = this.getEntryNext(item);
 			if(this.getEntryEquals(item, key, hash)){
 				if(last == null){
 					this.table[index] = next;
 				}else{
 					this.setEntryNext(last, next);
 				}
 				this.size--;
 				if(!verifyLength) return item;
 				this.verifyLength();
 				return item;
 			}

 		}
 		return null;
 	}
 
 	/**
 	 * Diese Methode aktualisiert die Größe der Tabelle mit der via {@link Hash#getLength(int, int)} berechneten.
 	 * 
 	 * @see Hash#verifyLength(int)
 	 */
 	protected final void verifyLength() {
 		this.verifyLength(this.getLength(this.size, this.table.length));
 	}
 
 	/**
 	 * Diese Methode aktualisiert die Größe der Tabelle, sofern die gegebene Größe ungleich der Größe der bisherigen
 	 * Tabelle ist. Bei der Aktialisierung werden alle Schlüssel-Wert-Paare der bisherigen Tabelle in eine neue Tabelle
 	 * der gegebenen Größe einfügt und die bisherige Tabelle mit der neuen ersetzt.
 	 * 
 	 * @param newLength neue Größe der Tabelle.
 	 */
 	@SuppressWarnings ("unchecked")
 	protected final void verifyLength(final int newLength) {
 		final GEntry[] oldTable = (GEntry[])this.table;
 		final int oldLength = oldTable.length;
 		if(oldLength == newLength) return;
 		final GEntry[] newTable = (GEntry[])(this.table = new Object[newLength]);
		if(newLength == 0) return;
 		for(int oldIndex = 0; oldIndex < oldLength; oldIndex++){
 			for(GEntry item = oldTable[oldIndex], next; item != null; item = next){
 				next = this.getEntryNext(item);
 				final GKey key = this.getEntryKey(item);
 				final int hash = this.getKeyHash(key);
 				final int newIndex = this.getIndex(hash, newLength);
 				newTable[newIndex] = this.createEntry(key, this.getEntryValue(item), newTable[newIndex], hash);
 			}
 		}
 	}
 
 	/**
 	 * Diese Methode gibt einen {@link Iterator} über die Einträge zurück.
 	 * 
 	 * @return {@link Iterator} über die Einträge.
 	 */
 	protected final Iterator<GEntry> getEntries() {
 		return ((this.size == 0) ? Iterators.<GEntry>voidIterator() : new HashIterator<GKey, GEntry>(this));
 	}
 
 }
