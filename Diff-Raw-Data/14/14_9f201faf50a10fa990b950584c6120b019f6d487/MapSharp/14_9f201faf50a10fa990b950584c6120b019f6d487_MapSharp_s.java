 package com.github.detentor.codex.collections.mutable;
 
 import java.io.Serializable;
 import java.util.Comparator;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
import java.util.TreeSet;
 
 import com.github.detentor.codex.collections.AbstractSharpCollection;
 import com.github.detentor.codex.collections.Builder;
 import com.github.detentor.codex.collections.SharpCollection;
 import com.github.detentor.codex.collections.mutable.ListSharp.ArrayBuilder;
 import com.github.detentor.codex.function.Function1;
 import com.github.detentor.codex.function.PartialFunction1;
 import com.github.detentor.codex.product.Tuple2;
 
 /**
  * Essa classe representa um mapa mutável, cujos elementos são armazenados num HashMap usando composição. <br/>
  * 
  * Os métodos desta classe são utilizados para favorecer a programação funcional, e possuem a mesma nomeclatura encontrada no Scala. Os
  * métodos desta classe são extremamente poderosos, e simplificam a codificação de estruturas mais complexas.<br/>
  * 
  * @author Vinícius Seufitele Pinto
  */
 public class MapSharp<K, V> extends AbstractSharpCollection<Tuple2<K, V>, MapSharp<K, V>> implements PartialFunction1<K, V>, Serializable
 {
 	private static final long serialVersionUID = 1L;
 
 	private final Map<K, V> backingMap;
 
 	/**
 	 * Construtor privado. Instâncias devem ser criadas com o 'from'
 	 */
 	protected MapSharp()
 	{
 		this(new HashMap<K, V>());
 	}
 	
 	/**
 	 * Construtor privado. Instâncias devem ser criadas com o 'from'
 	 */
 	protected MapSharp(final Map<K, V> fromMap)
 	{
 		super();
 		backingMap = fromMap;
 	}
 
 	@Override
 	public V apply(final K param)
 	{
 		return backingMap.get(param);
 	}
 
 	@Override
 	public boolean isDefinedAt(final K forValue)
 	{
 		return backingMap.containsKey(forValue);
 	}
 
 	/**
 	 * Cria uma instância de MapSharp a partir do mapa passado como parâmetro. <br/>
 	 * Os elementos do mapa serão copiados para o MapSharp, portanto mudanças estruturais 
 	 * no mapa original não afetarão este mapa.
 	 * 
 	 * @param T O tipo de dados da chave
 	 * @param U O tipo de dados do valor
 	 * @param theMap O mapa a partir do qual este mapa será criado
 	 * @return Um MapSharp que contém os elementos do mapa passado como parâmetro
 	 */
 	public static <T, U> MapSharp<T, U> from(final Map<T, U> theMap)
 	{
 		final MapSharp<T, U> retorno = new MapSharp<T, U>();
 
 		for (final Entry<T, U> ele : theMap.entrySet())
 		{
 			retorno.add(Tuple2.from(ele.getKey(), ele.getValue()));
 		}
 		return retorno;
 	}
 
 	/**
 	 * Cria uma instância de MapSharp a partir dos elementos existentes no iterable passado como parâmetro. 
 	 * A ordem da adição dos elementos será a mesma ordem do iterable.
 	 * 
 	 * @param <T> O tipo de dados da chave
 	 * @param <U> O tipo de dados do valor
 	 * @param theIterable O iterator que contém os elementos
 	 * @return Um mapa criado a partir da adição de todos os elementos do iterador
 	 */
 	public static <T, U> MapSharp<T, U> from(final Iterable<Tuple2<T, U>> theIterable)
 	{
 		final MapSharp<T, U> retorno = new MapSharp<T, U>();
 
 		for (final Tuple2<T, U> ele : theIterable)
 		{
 			retorno.add(ele);
 		}
 		return retorno;
 	}
 	
 	/**
 	 * Constrói uma instância de MapSharp vazia.
 	 * @param <T> O tipo da chave
 	 * @param <U> O tipo dos valores 
 	 * @return Uma instância de MapSharp vazia.
 	 */
 	public static <T, U> MapSharp<T, U> empty()
 	{
 		return new MapSharp<T, U>();
 	}
 	
 	/**
 	 * Constrói uma instância de MapSharp vazia, baseado no tipo passado como parâmetro.
 	 * 
 	 * @param mapType O tipo de MapSharp a ser construído
 	 * @param <T> O tipo de dados da instância
 	 * @return Uma instância de MapSharp vazia, do tipo definido.
 	 */
 	public static <T, U> MapSharp<T, U> empty(final MapSharpType mapType)
 	{
 		Map<T, U> mapInstance = null;
 		
 		switch(mapType)
 		{
 			case HASH_MAP:
 				mapInstance = new HashMap<T, U>();
 				break;
 			case LINKED_HASH_MAP:
 				mapInstance = new LinkedHashMap<T, U>();
 				break;
 			case TREE_MAP:
 				mapInstance = new TreeMap<T, U>();
 				break;
 			default:
 				throw new IllegalArgumentException("Tipo de MapSharp não reconhecido");
 		}
 		return new MapSharp<T, U>(mapInstance);
 	}
 
 	@Override
 	public int size()
 	{
 		return backingMap.size();
 	}
 
 	@Override
 	public Iterator<Tuple2<K, V>> iterator()
 	{
 		// Cria um iterador que aponta para o iterador do mapa
 		return new Iterator<Tuple2<K, V>>()
 		{
 			private final Iterator<Entry<K, V>> originalIte = backingMap.entrySet().iterator();
 
 			@Override
 			public boolean hasNext()
 			{
 				return originalIte.hasNext();
 			}
 
 			@Override
 			public Tuple2<K, V> next()
 			{
 				final Entry<K, V> entry = originalIte.next();
 				return Tuple2.from(entry.getKey(), entry.getValue());
 			}
 
 			@Override
 			public void remove()
 			{
 				originalIte.remove();
 			}
 		};
 	}
 	
 	/**
 	 * Retorna o conjunto de chaves contido neste mapa, como uma instância de {@link SetSharp}
 	 * @return Uma instância de SetSharp que contém as chaves deste mapa
 	 */
 	public SetSharp<K> keySet()
 	{
 		return SetSharp.from(backingMap.keySet());
 	}
 
 	/**
 	 * Retorna os valores contidos neste mapa, como uma instância de {@link SharpCollection}
 	 * @return Uma coleção que contém os valores do mapa.
 	 */
 	public SharpCollection<V> values()
 	{
 		return ListSharp.from(backingMap.values());
 	}
 
 	/**
 	 * Adiciona o elemento para este mapa, onde o val1 é a chave, e val2 o valor
 	 * 
 	 * @param element O elemento que representa o valor a ser adicionado
 	 * @return A referência a este mapa após a adição
 	 */
 	public MapSharp<K, V> add(final Tuple2<K, V> element)
 	{
 		backingMap.put(element.getVal1(), element.getVal2());
 		return this;
 	}
 
 	public MapSharp<K, V> clear()
 	{
 		backingMap.clear();
 		return this;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public <B> Builder<B, SharpCollection<B>> builder()
 	{
 		Builder<B, SharpCollection<B>> builderRetorno = null;
 		
		if (backingMap instanceof HashSet<?>)
 		{
 			builderRetorno = new HashMapBuilder();
 		}
		else if (backingMap instanceof LinkedHashSet<?>)
 		{
 			builderRetorno = new LinkedHashMapBuilder();
 		}
		else if (backingMap instanceof TreeSet<?>)
 		{
 			builderRetorno = new TreeMapBuilder();
 		}
 		else
 		{
 			throw new IllegalArgumentException("Tipo de instância não reconhecida");
 		}
 		return builderRetorno;
 	}
 	
 	/**
 	 * ATENÇÃO: O método contains verifica se este mapa, representado por uma coleção de tuplas, 
 	 * contém a tupla passada como parâmetro. Para verificar se ele possui a chave, use o método {@link #containsKey containsKey}.
 	 */
 	@Override
 	public boolean contains(final Tuple2<K, V> element)
 	{
 		return super.contains(element);
 	}
 
 	/**
 	 * ATENÇÃO: O método contains verifica se este mapa, representado por uma coleção de tuplas, 
 	 * contém todos os elementos de uma outra coleção de tuplas.
 	 */
 	@Override
 	public boolean containsAll(final SharpCollection<Tuple2<K, V>> col)
 	{
 		return super.containsAll(col);
 	}
 
 	/**
 	 * Remove, deste mapa, o elemento com a chave passada como parâmetro, se ela existir
 	 * 
 	 * @param element A chave do elemento a ser removido
 	 * @return A referência a este mapa, após a remoção
 	 */
 	public MapSharp<K, V> removeKey(final K key)
 	{
 		backingMap.remove(key);
 		return this;
 	}
 
 	/**
 	 * Adiciona o elemento passado como parâmetro nesta coleção. <br/><br/>
 	 * 
 	 * @param key A chave do elemento a ser adicionado
 	 * @param value O valor do elemento a ser adicionado
 	 * @return A referência à coleção, após o elemento ser adicionado.
 	 */
 	public MapSharp<K, V> add(final K key, final V value)
 	{
 		backingMap.put(key, value);
 		return this;
 	}
 
 	/**
 	 * Retorna o valor para o qual a chave especificada está mapeada, ou {@code null} se 
 	 * este mapa não contém mapeamento para a chave. <br/><br/>
 	 * 
 	 * Formalmente, se este mapa contém um mapeamento de uma chave {@code k} para um valor {@code v} tal que
 	 * {@code (key==null ? k==null : key.equals(k))}, então este método retorna {@code v}; 
 	 * do contrário ele retorna {@code null}. (Há, no máximo um mapeamento) <br/><br/>
 	 * 
 	 * Um valor de retorno {@code null} não significa, necessariamente, que o mapa não contém 
 	 * mapeamento para a chave: é também possível que o mapa explicitamente mapeou a chave para {@code null}. 
 	 * A operação {@link #containsKey containsKey} pode ser utilizada para distinguir entre os dois casos.
 	 */
 	public V get(final K key)
 	{
 		return backingMap.get(key);
 	}
 
 	/**
 	 * Faz uma tentativa de recuperar o valor para uma determinada chave. <br/>
 	 * Se o valor não estiver mapeado no mapa, retorna a parte do else ao invés.
 	 * 
 	 * @param key A chave a ser procurado o valor
 	 * @param elsePart O valor a ser retornado, caso não exista
 	 * @return O valor mapeado pela chave, ou a parte do else
 	 */
 	public V getOrElse(final K key, final V elsePart)
 	{
 		final V value = backingMap.get(key);
 
 		if (value != null || backingMap.containsKey(key))
 		{
 			return value;
 		}
 		return elsePart;
 	}
 
 	/**
 	 * Retorna <tt>true</tt> se este mapa contém um mapeamento para a chave especificada
 	 * 
 	 * @param key A chave cuja presença no mapa será testada
 	 * @return <tt>true</tt> se este mapa contém um mapeamento para a chave
 	 */
 	public boolean containsKey(final K key)
 	{
 		return backingMap.containsKey(key);
 	}
 
 	/**
 	 * Retorna <tt>true</tt> se este mapa contém pelo menos um chave mapeada para o valor especificado.
 	 * 
 	 * @param value Valor cuja presença no mapa será testada
 	 * @return <tt>true</tt> se este mapa contém pelo menos uma chave mapeada para o valor
 	 */
 	public boolean containsValue(final V value)
 	{
 		return backingMap.containsValue(value);
 	}
 	
 	/**
 	 * Constrói uma nova coleção a partir da aplicação da função passada como parâmetro em cada elemento da coleção. <br/>
 	 * A ordem é preservada, se ela estiver bem-definida.
 	 * @param <B> O tipo da nova coleção.
 	 * @param function Uma função que recebe um elemento desta coleção, e retorna um elemento de (potencialmente) outro tipo.
 	 * @return Uma nova coleção, a partir da aplicação da função para cada elemento.
 	 */
 	@SuppressWarnings("unchecked")
 	public <W, Y> MapSharp<W, Y> map(final Function1<Tuple2<K, V>, Tuple2<W, Y>> function)
 	{
 		return (MapSharp<W, Y>) super.map(function);
 	}
 
 	@Override
 	public <B> SharpCollection<B> map(final Function1<? super Tuple2<K, V>, B> function)
 	{
 		final Builder<B, SharpCollection<B>> colecaoRetorno = new ArrayBuilder<B>();
 
 		for (final Tuple2<K, V> ele : this)
 		{
 			colecaoRetorno.add(function.apply(ele));
 		}
 		return colecaoRetorno.result();
 	}
 
 	/**
 	 * Constrói uma nova coleção a partir da aplicação da função parcial passada como parâmetro em cada elemento da coleção
 	 * onde a função parcial está definida. A ordem é preservada, se ela estiver bem-definida. <br/><br/>
 	 * Nos casos onde é necessário usar um filtro antes de aplicar um mapa, considere utilizar esta função. <br/>
 	 * @param <B> O tipo da nova coleção.
 	 * @param pFunction Uma função que recebe um elemento desta coleção, e retorna um elemento de (potencialmente) outro tipo.
 	 * @return Uma nova coleção, a partir da aplicação da função parcial para cada elemento onde ela está definida.
 	 */
 	@SuppressWarnings("unchecked")
 	public <W, Y> MapSharp<W, Y> collect(final PartialFunction1<Tuple2<K, V>, Tuple2<W, Y>> pFunction)
 	{
 		return (MapSharp<W, Y>) super.collect(pFunction);
 	}
 
 	@Override
 	public <B> SharpCollection<B> collect(final PartialFunction1<? super Tuple2<K, V>, B> pFunction)
 	{
 		final Builder<B, SharpCollection<B>> colecaoRetorno = new ArrayBuilder<B>();
 
 		for (final Tuple2<K, V> ele : this)
 		{
 			if (pFunction.isDefinedAt(ele))
 			{
 				colecaoRetorno.add(pFunction.apply(ele));
 			}
 		}
 		return colecaoRetorno.result();
 	}
 	
 	/**
 	 * Constrói uma nova coleção, a partir da aplicação da função passada 
 	 * como parâmetro em cada elemento da coleção, coletando os resultados numa
 	 * única coleção. <br/>
 	 * @param <B> O tipo da nova coleção
 	 * @param function Uma função que recebe um elemento desta coleção, e retorna uma 
 	 * coleção de elementos de (potencialmente) outro tipo.
 	 * @return Uma nova coleção, a partir da aplicação da função para cada elemento, concatenando os elementos
 	 * das coleção.
 	 */
 	@SuppressWarnings("unchecked")
 	public <W, Y> MapSharp<W, Y> flatMap(final Function1<Tuple2<K, V>, ? extends SharpCollection<Tuple2<W, Y>>> function)
 	{
 		return (MapSharp<W, Y>) super.flatMap(function);
 	}
 
 	@Override
 	public <B> SharpCollection<B> flatMap(final Function1<? super Tuple2<K, V>, ? extends SharpCollection<B>> function)
 	{
 		final Builder<B, SharpCollection<B>> colecaoRetorno = new ArrayBuilder<B>();
 
 		for (final Tuple2<K, V> ele : this)
 		{
 			for (final B mappedEle : function.apply(ele))
 			{
 				colecaoRetorno.add(mappedEle);
 			}
 		}
 		return colecaoRetorno.result();
 	}
 
 	/**
 	 * Retorna esta coleção como um mapa do Java. As modificações no mapa retornado afetam também este mapa.
 	 * 
 	 * @return O mapa que representa esta coleção
 	 */
 	public Map<K, V> toMap()
 	{
 		return backingMap;
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (backingMap == null ? 0 : backingMap.hashCode());
 		return result;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public boolean equals(final Object obj)
 	{
 		if (this == obj)
 		{
 			return true;
 		}
 		if (obj == null)
 		{
 			return false;
 		}
 		if (getClass() != obj.getClass())
 		{
 			return false;
 		}
 		final MapSharp other = (MapSharp) obj;
 		if (backingMap == null)
 		{
 			if (other.backingMap != null)
 			{
 				return false;
 			}
 		}
 		else if (!backingMap.equals(other.backingMap))
 		{
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString()
 	{
 		return backingMap.toString();
 	}
 	
 	/**
 	 * Essa classe é um builder para Set baseado em um MapSharp. <br/>
 	 * @param <K, V> K é o tipo de dados da chave, V é o tipo de dados do valor.
 	 */
 	private class HashMapBuilder<X,Y> implements Builder<Tuple2<X, Y>, SharpCollection<Tuple2<X, Y>>>
 	{
 		private final Map<X, Y> hashMap = new HashMap<X, Y>();
 
 		@Override
 		public void add(final Tuple2<X, Y> element)
 		{
 			hashMap.put(element.getVal1(), element.getVal2());
 		}
 
 		@Override
 		public MapSharp<X, Y> result()
 		{
 			return new MapSharp<X, Y>(hashMap);
 		}
 	}
 	
 	/**
 	 * Essa classe é um builder para MapSharp baseado no LinkedHashMap. <br/>
 	 * @param <K, V> K é o tipo de dados da chave, V é o tipo de dados do valor.
 	 */
 	private class LinkedHashMapBuilder<X,Y> implements Builder<Tuple2<X, Y>, SharpCollection<Tuple2<X, Y>>>
 	{
 		private final Map<X, Y> hashMap = new LinkedHashMap<X, Y>();
 
 		@Override
 		public void add(final Tuple2<X, Y> element)
 		{
 			hashMap.put(element.getVal1(), element.getVal2());
 		}
 
 		@Override
 		public MapSharp<X, Y> result()
 		{
 			return new MapSharp<X, Y>(hashMap);
 		}
 	}
 	
 	/**
 	 * Essa classe é um builder para MapSharp baseado no TreeMap. <br/>
 	 * @param <K, V> K é o tipo de dados da chave, V é o tipo de dados do valor.
 	 */
 	private class TreeMapBuilder<X,Y> implements Builder<Tuple2<X, Y>, SharpCollection<Tuple2<X, Y>>>
 	{
 		private final Map<X, Y> hashMap = new TreeMap<X, Y>();
 
 		@Override
 		public void add(final Tuple2<X, Y> element)
 		{
 			hashMap.put(element.getVal1(), element.getVal2());
 		}
 
 		@Override
 		public MapSharp<X, Y> result()
 		{
 			return new MapSharp<X, Y>(hashMap);
 		}
 	}
 	
 	public enum MapSharpType
 	{
 		HASH_MAP, LINKED_HASH_MAP, TREE_MAP;
 	}
 
 	/**
 	 * Ordena o mapa, de acordo com a ordenação natural das chaves.  <br/>
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public MapSharp<K, V> sorted()
 	{
 		return (MapSharp<K, V>) sorted(new DefaultComparator());
 	}
 
 	/**
 	 * Ordena o mapa, de acordo com a ordenação definida pelo comparator passado como parâmetro.  <br/>
 	 * ATENÇÃO: O mapa retornado será uma instância de LinkedHashMap, 
 	 * portanto a adição de novos elementos poderá, potencialmente, corromper a ordenação. 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public MapSharp<K, V> sorted(final Comparator<? super Tuple2<K, V>> comparator)
 	{
 		final MapSharp<K, V> mapaRetorno = empty(MapSharpType.LINKED_HASH_MAP);
 		
 		for (Tuple2<K, V> curEle : ListSharp.from(this).sorted(comparator))
 		{
 			mapaRetorno.add(curEle);
 		}
 		return mapaRetorno;
 	}
 }
