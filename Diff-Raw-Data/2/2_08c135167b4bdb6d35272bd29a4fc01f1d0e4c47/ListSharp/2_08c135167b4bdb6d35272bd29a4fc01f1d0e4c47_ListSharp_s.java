 package com.github.detentor.codex.collections.immutable;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.github.detentor.codex.collections.AbstractIndexedSeq;
 import com.github.detentor.codex.collections.Builder;
 import com.github.detentor.codex.collections.SharpCollection;
 import com.github.detentor.codex.collections.builders.ImArrayBuilder;
 import com.github.detentor.codex.function.Function1;
 import com.github.detentor.codex.function.PartialFunction;
 import com.github.detentor.codex.product.Tuple2;
 
 /**
  * Implementação de ListSharp imútável. <br/>
  * Sempre que possível, deve-se favorecer essa implementação em detrimento da mutável, pois apresenta os seguintes ganhos: <br/>
  * 
  * 1 - Totalmente thread-safe, por ser imutável. <br/>
  * 2 - Performance muito superior para a grande parte das operações. <br/>
  * 3 - Custo de memória constante ao lidar com sub-listas (padrão Flyweight, assim como String). <br/>
  * 
  * 
  * @author f9540702 Vinícius Seufitele Pinto
  * 
  * @param <T>
  */
 public class ListSharp<T> extends AbstractIndexedSeq<T, ListSharp<T>> implements Serializable
 {
 	private static final long serialVersionUID = 1L;
 
 	private final int startIndex;
 	private final int theSize;
 
 	private final Object[] data;
 
 	// Singleton, pois como é imutável não faz sentido criar várias
 	private static final ListSharp<Object> EMPTY_LIST = new ListSharp<Object>();
 
 	/**
 	 * Construtor privado. Instâncias devem ser criadas com o 'from'
 	 */
 	protected ListSharp()
 	{
 		startIndex = 0;
 		theSize = 0;
 		data = new Object[0];
 	}
 
 	/**
 	 * Construtor privado, que reutiliza o objeto theData passado.
 	 */
 	protected ListSharp(final Object[] theData)
 	{
 		this(theData, 0, theData.length);
 	}
 
 	/**
 	 * Construtor privado, que reutiliza o objeto theData passado.
 	 * 
 	 * @param theData
 	 * @param theStart Representa onde vai começar o índice da lista
 	 * @param theEnd Representa onde vai terminar a lista (exclusive)
 	 */
 	protected ListSharp(final Object[] theData, final int theStart, final int theEnd)
 	{
 		startIndex = theStart;
 		theSize = theEnd - theStart;
 		data = theData;
 	}
 
 	/**
 	 * Construtor privado, que reutiliza o objeto passado.
 	 * 
 	 * @param prevList
 	 * @param theStart Representa onde vai começar o índice da lista
 	 * @param theEnd Representa onde vai terminar a lista (exclusive)
 	 */
 	protected ListSharp(final ListSharp<T> prevList, final int theStart, final int theEnd)
 	{
		startIndex = theStart;
 		theSize = theEnd - theStart;
 		data = prevList.data;
 	}
 
 	/**
 	 * Constrói uma instância de ListSharp vazia.
 	 * 
 	 * @param <A> O tipo de dados da instância
 	 * @return Uma instância de ListSharp vazia.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <A> ListSharp<A> empty()
 	{
 		// Retorna sempre a mesma lista - afinal, ela é imutável
 		return (ListSharp<A>) EMPTY_LIST;
 	}
 
 	/**
 	 * Cria uma instância de ListSharp a partir dos elementos existentes no iterable passado como parâmetro. A ordem da adição dos
 	 * elementos será a mesma ordem do iterable.
 	 * 
 	 * @param <T> O tipo de dados da lista
 	 * @param theIterable O iterator que contém os elementos
 	 * @return Uma lista criada a partir da adição de todos os elementos do iterador
 	 */
 	public static <T> ListSharp<T> from(final Iterable<T> theIterable)
 	{
 		// por enquanto está bem porco, melhorar
 		final List<T> listaRetorno = new ArrayList<T>();
 
 		for (final T ele : theIterable)
 		{
 			listaRetorno.add(ele);
 		}
 		return new ListSharp<T>(listaRetorno.toArray());
 	}
 
 	/**
 	 * Cria uma nova ListSharp, a partir dos valores passados como parâmetro. <br/>
 	 * Esse método é uma forma mais compacta de se criar ListSharp.
 	 * 
 	 * @param <A> O tipo de dados da ListSharp a ser retornada.
 	 * @param collection A ListSharp a ser criada, a partir dos valores
 	 * @return Uma nova ListSharp, cujos elementos são os elementos passados como parâmetro
 	 */
 	public static <T> ListSharp<T> from(final T... valores)
 	{
 		return new ListSharp<T>(Arrays.copyOf(valores, valores.length));
 	}
 
 	@Override
 	public int size()
 	{
 		return theSize;
 	}
 
 	@Override
 	public ListSharp<T> subsequence(final int startIndex, final int endIndex)
 	{
 		return new ListSharp<T>(this, Math.max(startIndex, 0), Math.min(endIndex, this.size()));
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public T apply(final Integer param)
 	{
 		return (T) data[startIndex + param];
 	}
 
 	@Override
 	public <B> Builder<B, SharpCollection<B>> builder()
 	{
 		return new ImArrayBuilder<B>();
 	}
 
 	@Override
 	public <B> ListSharp<B> map(final Function1<? super T, B> function)
 	{
 		return new ListSharp<B>(data, startIndex, this.startIndex + this.size())
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public B apply(Integer param)
 			{
 				return function.apply(ListSharp.this.apply(param));
 			}
 		};
 	}
 
 	@Override
 	public <B> ListSharp<B> collect(final PartialFunction<? super T, B> pFunction)
 	{
 		return (ListSharp<B>) super.collect(pFunction);
 	}
 
 	@Override
 	public <B> ListSharp<B> flatMap(final Function1<? super T, ? extends SharpCollection<B>> function)
 	{
 		return (ListSharp<B>) super.flatMap(function);
 	}
 
 	@Override
 	public ListSharp<Tuple2<T, Integer>> zipWithIndex()
 	{
 		return (ListSharp<Tuple2<T, Integer>>) super.zipWithIndex();
 	}
 
 	@Override
 	public String toString()
 	{
 		return mkString("[", ", ", "]");
 	}
 }
